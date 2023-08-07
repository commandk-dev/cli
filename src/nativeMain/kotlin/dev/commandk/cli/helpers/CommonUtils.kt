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
}