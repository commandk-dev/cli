package dev.commandk.cli.commands

import arrow.core.Either
import arrow.core.raise.either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.defaultByName
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.kgit2.process.Command
import dev.commandk.cli.api.CommandKApi
import dev.commandk.cli.common.environmentOption
import dev.commandk.cli.common.identifierTypeOption
import dev.commandk.cli.common.subTypeOption
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.context.executeCliCommand
import dev.commandk.cli.helpers.CommonUtils
import dev.commandk.cli.helpers.FormatUtil
import dev.commandk.cli.models.CliError
import dev.commandk.cli.options.EnvVarRunType
import dev.commandk.cli.options.FileStoreRunType
import platform.posix.system

class RunCommand(
    private val commandKApiProvider: (CommonContext) -> CommandKApi,
) : CliktCommand("Fetch secrets, and invoke the application runner", name = "run") {
    private val commonContext by requireObject<CommonContext>()
    private val formatUtil = FormatUtil()
    private val applicationName by argument(help = "The application name to get secrets for", name = "application-name")
    private val environment by environmentOption()
    private val applicationSubType by subTypeOption()
    private val identifierType by identifierTypeOption()
    private val command by argument(
        help = "The command to run the application",
    ).multiple()
    private val commonUtils = CommonUtils(commandKApiProvider)


    private val runType by option("--run-type", help = "The type of run to perform")
        .groupChoice(
            "file-store" to FileStoreRunType(),
            "env-var" to EnvVarRunType()
        ).defaultByName("env-var")

    override fun run() {
        commonContext.executeCliCommand {
            runInternal()
        }
    }

    private suspend fun runInternal(): Either<CliError, Unit> {
        return either {
            val environment = commonUtils.findEnvironmentByNameOrSlug(commonContext, environment).bind()
            cc().writeLine("✅ Using environment ${environment.name} [id: ${environment.id}]")

            val applicationId = if (identifierType == "Name") {
                commandKApiProvider(commonContext).getCatalogApp(applicationName, applicationSubType)
                    .bind()
                    .id
            } else {
                applicationName
            }
            cc().writeLine("Using application with UUID ${TextColors.green("[$applicationId]")}")

            val renderedSecrets = commandKApiProvider(commonContext).getRenderedSecrets(
                applicationId,
                environment
            ).bind()

            val envsPrefix = when (runType) {
                is FileStoreRunType -> {
                    val fileFormat = (runType as FileStoreRunType).fileFormat
                    val fileName = (runType as FileStoreRunType).fileName
                    val secrets = formatUtil.formatSecrets(renderedSecrets.secrets, fileFormat)
                    commonUtils.writeToFile(fileName, secrets).bind()
                    cc().writeLine("✅ Secrets fetched and written to file ${TextColors.green(fileName)}")
                    emptyList()
                }

                is EnvVarRunType ->  {
                    cc().writeLine("✅ Secrets fetched and will be set as environment variables")
                    renderedSecrets.secrets.map { renderedSecret ->
                        "${renderedSecret.key}='${renderedSecret.serializedValue.replace("'", "'\\''")}'"
                    }
                }
            }.joinToString(" ")

            system("$envsPrefix ${command.joinToString(" ")}")
        }
    }
}
