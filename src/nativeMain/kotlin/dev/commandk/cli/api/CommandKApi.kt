package dev.commandk.cli.api

import arrow.core.Either
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKApp
import dev.commandk.cli.models.CommandKEnvironment
import dev.commandk.cli.models.CommandKEnvironments
import dev.commandk.cli.models.CommandKProviders
import dev.commandk.cli.models.NetworkError
import dev.commandk.cli.models.CommandKAppRenderedSecrets

interface CommandKApi {
    suspend fun getCatalogApp(appName: String): Either<CliError, CommandKApp>
    suspend fun getProviders(): Either<NetworkError, CommandKProviders>
    suspend fun getEnvironments(): Either<NetworkError, CommandKEnvironments>
    suspend fun getRenderedSecrets(
        applicationId: String,
        commandKEnvironment: CommandKEnvironment
    ): Either<NetworkError, CommandKAppRenderedSecrets>
    suspend fun writeSecretValue(
        applicationId: String,
        commandKEnvironment: CommandKEnvironment,
        providerId: String,
        secretName: String,
        secretValue: String
    ): Either<NetworkError, Unit>
}
