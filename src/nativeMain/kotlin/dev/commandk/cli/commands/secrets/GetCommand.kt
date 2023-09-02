package dev.commandk.cli.commands.secrets

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import dev.commandk.cli.api.CommandKApi
import dev.commandk.cli.common.applicationNameArgument
import dev.commandk.cli.common.environmentOption
import dev.commandk.cli.common.identifierTypeOption
import dev.commandk.cli.common.outputFormatOption
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.context.executeCliCommand
import dev.commandk.cli.helpers.CommonUtils
import dev.commandk.cli.helpers.FormatUtil
import dev.commandk.cli.models.CentralDataError
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKEnvironment

class GetCommand(
    private val commandKApiProvider: (CommonContext) -> CommandKApi
) : CliktCommand("Get Secrets for an app and environment", name = "get") {
    private val commonContext by requireObject<CommonContext>()
    private val commonUtils = CommonUtils(commandKApiProvider)
    private val formatUtil = FormatUtil()
    private val environment by environmentOption()
    private val applicationName by applicationNameArgument(
        help = "The application name to fetch secrets for"
    )
    private val outputFormat by outputFormatOption()
    private val outputFilename by option(
        "--output-file-name",
        help = "The name of the file to write secrets to"
    ).required()
    private val identifierType by identifierTypeOption()
    override fun run() {
        commonContext.executeCliCommand {
            runInternal()
        }
    }

    private suspend fun runInternal(): Either<CliError, Unit> {
        return either {
            val environment = getEnvironment().bind()

            val applicationId = if (identifierType == "Name") {
                commandKApiProvider(commonContext).getCatalogApp(applicationName)
                    .bind()
                    .id
            } else {
                applicationName
            }

            val renderedSecrets = commandKApiProvider(commonContext).getRenderedSecrets(
                applicationId,
                environment
            ).bind()

            val secrets = formatUtil.formatSecrets(renderedSecrets.secrets, outputFormat)
            cc().writeLine("Writing fetched secrets to file `$outputFilename`")
            commonUtils.writeToFile(outputFilename, secrets)
                .bind()
        }
    }

    private suspend fun getEnvironment(): Either<CliError, CommandKEnvironment> {
        return commandKApiProvider(commonContext).getEnvironments()
            .map { (environments) ->
                environments.find { it.name == environment || it.slug == environment }
            }
            .flatMap { it?.right() ?: CentralDataError.EnvironmentNotFound("name:$environment").left() }

    }
}
