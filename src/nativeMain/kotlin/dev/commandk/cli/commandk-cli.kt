package dev.commandk.cli // ktlint-disable filename

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.sources.MapValueSource
import com.github.ajalt.mordant.terminal.Terminal
import dev.commandk.cli.api.DefaultCommandKApi
import dev.commandk.cli.commands.secrets.ImportCommand
import dev.commandk.cli.commands.TestCommandk
import dev.commandk.cli.commands.secrets.GetCommand
import dev.commandk.cli.commands.RunCommand
import dev.commandk.cli.commands.VersionCommand
import dev.commandk.cli.commands.secrets.SecretsCommand
import dev.commandk.cli.common.CommonConfigurationValues
import dev.commandk.cli.common.CommonEnvironmentVars
import dev.commandk.cli.context.AccessAuthorizationParameters
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.services.ConfigPropertiesLoader
import io.ktor.http.content.Version
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

class CommandK(
    private val configFileLocation: String?,
) : CliktCommand("cmdk", autoCompleteEnvvar = CommonEnvironmentVars.CompletionScript, name = "cmdk") {
    companion object {
        const val ApplicationName = "commandk-cli"
        const val Version = "0.1.3"
    }

    private val loadedConfig: Map<String, String>
    private val configPropertiesLoader = ConfigPropertiesLoader()
    private val apiAccessToken by option(help = "The API Access Token used to make API calls", envvar = CommonEnvironmentVars.ApiAccessToken)
        .required()
    private val apiEndpoint by option(help = "The HTTPs endpoint for the CommandK API Server", envvar = CommonEnvironmentVars.ApiEndpoint)
        .required()

    private val baseConfig = mapOf(
        "api-endpoint" to CommonConfigurationValues.DefaultApiEndpoint
    )

    init {
        loadedConfig = configFileLocation?.let { configFile -> configPropertiesLoader.loadProperties(configFile) }
            ?: emptyMap()

        context {
            configFileLocation?.let {
                valueSource = MapValueSource(baseConfig + loadedConfig)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun run() {
        currentContext.obj = CommonContext(
            Terminal(),
            if (getenv("CMDK_USE_API_TOKEN_AS_SESSION_TOKEN")?.toKString().toBoolean()) {
                AccessAuthorizationParameters.UserSessionToken(apiAccessToken)
            } else {
                AccessAuthorizationParameters.ApiAccessToken(apiAccessToken)
            },
            apiEndpoint,
        )
    }
}

val commandKApiProvider = { context: CommonContext -> DefaultCommandKApi(context) }

@OptIn(ExperimentalForeignApi::class)
val subcommands = emptyList<CliktCommand>() +
    if (getenv("CMDK_ENABLE_TEST_COMMAND")?.toKString().toBoolean()) {
        listOf(TestCommandk())
    } else {
        emptyList()
    } +
    // Add all other subcommands here
    listOf(
        SecretsCommand()
            .subcommands(ImportCommand(commandKApiProvider), GetCommand(commandKApiProvider)),
        RunCommand(commandKApiProvider),
        VersionCommand(Terminal())
    )

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    if (args[0] == "version") {
        VersionCommand(Terminal())
            .run()
    } else {
        CommandK(
            (
                (getenv("CMDK_CONFIG_FILE")?.toKString())
                    ?: (getenv("HOME")?.let { "${it.toKString()}/.commandk.config.json" })
                ),
        ).subcommands(subcommands).main(args)
    }
}
