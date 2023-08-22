package dev.commandk.cli.helpers

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import dev.commandk.cli.api.CommandKApi
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.models.CentralDataError
import dev.commandk.cli.models.CliError
import dev.commandk.cli.models.CommandKEnvironment
import okio.FileSystem
import okio.Path.Companion.toPath

class CommonUtils(
    private val commandKApiProvider: (CommonContext) -> CommandKApi
) {
    suspend fun findEnvironmentByNameOrSlug(
        commonContext: CommonContext,
        environment: String
    ): Either<CliError, CommandKEnvironment> {
        return commandKApiProvider(commonContext).getEnvironments()
            .map { (environments) ->
                environments.find { it.name == environment || it.slug == environment }
            }
            .flatMap { it?.right() ?: CentralDataError.EnvironmentNotFound("name:$environment").left() }
    }

    fun writeToFile(filePath: String, data: String): Either<CliError, Unit> {
        // Write the secrets to the specified file
        FileSystem.SYSTEM.write(filePath.toPath()) {
            writeUtf8(data)
        }
        return Unit.right()
    }
}
