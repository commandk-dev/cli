package dev.commandk.cli.commands.secrets

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.defaultByName
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
import okio.FileSystem
import okio.Path.Companion.toPath

class RunCommand(
    private val commandKApiProvider: (CommonContext) -> CommandKApi
) : CliktCommand("Fetch secrets, and invoke the application runner", name = "run") {
    private val commonContext by requireObject<CommonContext>()
    private val formatUtil = FormatUtil()
    private val applicationName by argument(help = "The application name to get secrets for", name = "application-name")
    private val environment by environmentOption()
    private val applicationSubType by subTypeOption()
    private val identifierType by identifierTypeOption()
    private val applicationRunCommand by option(
        "--command",
        help = "The command to run the application",
    ).required()
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

            when (runType) {
                is FileStoreRunType -> {
                    val fileFormat = (runType as FileStoreRunType).fileFormat
                    val fileName = (runType as FileStoreRunType).fileName
                    val secrets = formatUtil.formatSecrets(renderedSecrets.secrets, fileFormat)
                    writeToFile(fileName, secrets).bind()
                    cc().writeLine("✅ Secrets fetched and written to file ${TextColors.green(fileName)}")
                }

                is EnvVarRunType ->  {
                    cc().writeLine("✅ Secrets fetched and will be set as environment variables")
                }
            }

            val args = applicationRunCommand.split(" ")
            val command = args.first()
            val commandArgs = args.drop(1)
            Command(command)
                .let {
                    when (runType) {
                        is EnvVarRunType -> it.envs(envs = renderedSecrets.secrets.map { (key, value) ->
                            key to value
                        }.toTypedArray())

                        is FileStoreRunType -> {
                            it
                        }
                    }
                }
                .args(args = commandArgs.toTypedArray())
                .spawn()
                .wait()
        }
    }

    private fun writeToFile(filePath: String, data: String): Either<CliError, Unit> {
        // Write the secrets to the specified file
        FileSystem.SYSTEM.write(filePath.toPath()) {
            writeUtf8(data)
        }
        return Unit.right()
    }
}
