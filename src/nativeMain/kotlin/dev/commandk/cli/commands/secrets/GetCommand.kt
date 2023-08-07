package dev.commandk.cli.commands.secrets

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import dev.commandk.cli.api.CommandKApi
import dev.commandk.cli.common.applicationNameOption
import dev.commandk.cli.common.environmentOption
import dev.commandk.cli.common.identifierTypeOption
import dev.commandk.cli.common.outputFormatOption
import dev.commandk.cli.common.subTypeOption
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.context.executeCliCommand
import dev.commandk.cli.helpers.FormatUtil
import dev.commandk.cli.models.CentralDataError
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKEnvironment

class GetCommand(
    private val commandKApiProvider: (CommonContext) -> CommandKApi
) : CliktCommand("Get Secrets for an app and environment", name = "get") {
    private val commonContext by requireObject<CommonContext>()
    private val formatUtil = FormatUtil()
    private val environment by environmentOption()
    private val applicationName by applicationNameOption()
    private val outputFormat by outputFormatOption()
    private val identifierType by identifierTypeOption()
    private val applicationSubType by subTypeOption()
    override fun run() {
        commonContext.executeCliCommand {
            runInternal()
        }
    }

    private suspend fun runInternal(): Either<CliError, Unit> {
        return either {
            val environment = getEnvironment().bind()

            val applicationId = if (identifierType == "Name") {
                commandKApiProvider(commonContext).getCatalogApp(applicationName, applicationSubType)
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
            cc().writeLine(secrets)
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
