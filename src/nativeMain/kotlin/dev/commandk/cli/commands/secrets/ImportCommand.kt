package dev.commandk.cli.commands.secrets

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.rendering.TextColors
import dev.commandk.cli.api.CommandKApi
import dev.commandk.cli.common.applicationNameArgument
import dev.commandk.cli.common.environmentOption
import dev.commandk.cli.common.identifierTypeOption
import dev.commandk.cli.common.subTypeOption
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.context.executeCliCommand
import dev.commandk.cli.helpers.CommonUtils
import dev.commandk.cli.models.CentralDataError
import dev.commandk.cli.models.CliError
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

class ImportCommand(
    private val commandKApiProvider: (CommonContext) -> CommandKApi
) : CliktCommand("Import multiple secrets to CommandK CLI", name = "import") {
    private val commonContext by requireObject<CommonContext>()
    private val applicationName by applicationNameArgument(
        help = "The name of the application to import secrets into"
    )
    private val environment by environmentOption()
    private val applicationSubType by subTypeOption()
    private val identifierType by identifierTypeOption()
    private val importFile by option("--import-file", help = "The file to import data from")
        .required()
    private val commonUtils = CommonUtils(commandKApiProvider)

    override fun run() {
        commonContext.executeCliCommand {
            runInternal()
        }
    }

    private suspend fun runInternal(): Either<CliError, Unit> {
        return either {
            val environment = commonUtils.findEnvironmentByNameOrSlug(commonContext, environment).bind()
            cc().writeLine("✅ Using environment ${environment.name} [id: ${environment.id}]")
            val secretsToUpload = readImportFile().bind()
            cc().writeLine("Read [${secretsToUpload.size}] secrets to upload")
            val providerId = getDefaultProviderId().bind()

            val applicationId = if (identifierType == "Name") {
                commandKApiProvider(commonContext).getCatalogApp(applicationName, applicationSubType)
                    .bind()
                    .id
            } else {
                applicationName
            }
            cc().writeLine("Using application with UUID ${TextColors.green("[$applicationId]")}")

            secretsToUpload.map {
                commandKApiProvider(commonContext).writeSecretValue(
                    applicationId,
                    environment,
                    providerId,
                    it.key,
                    it.value,
                )
            }.bindAll()
        }
    }

    private suspend fun getDefaultProviderId(): Either<CliError, String> {
        return commandKApiProvider(commonContext).getProviders()
            .map { (providers) ->
                providers.find { it.name == "Other" }
            }
            .flatMap { it?.id?.right() ?: CentralDataError.ProviderNotFound("Other").left() }
    }

    private suspend fun readImportFile(): Either<CliError, Map<String, String>> {
        val path = importFile.toPath(normalize = true)
        val fileContents = FileSystem.SYSTEM.read(path) {
            readUtf8()
        }

        return Json.decodeFromString<Map<String, String>>(fileContents)
            .right()
    }
}
