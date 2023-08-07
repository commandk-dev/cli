package dev.commandk.cli.context

import com.github.ajalt.mordant.terminal.Terminal

data class CommonContext(
    val terminal: Terminal,
    val accessAuthorizationParameters: AccessAuthorizationParameters,
    val apiEndpoint: String
)

sealed class AccessAuthorizationParameters {
    data class ApiAccessToken(
        val accessToken: String,
    ) : AccessAuthorizationParameters()

    data class UserSessionToken(
        val sessionToken: String
    ) : AccessAuthorizationParameters()
}
