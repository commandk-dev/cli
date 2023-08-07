package dev.commandk.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import dev.commandk.cli.context.AccessAuthorizationParameters
import dev.commandk.cli.context.CommonContext

class TestCommandk : CliktCommand("Run the test subcommand on CommandK CLI", name = "test") {
    private val commonContext by requireObject<CommonContext>()

    override fun run() {
        println(
            "${TextStyles.bold(TextColors.red("Running"))} command [apiAccessToken='" +
                "${TextColors.green((commonContext.accessAuthorizationParameters as AccessAuthorizationParameters.ApiAccessToken).accessToken)}'," +
                " apiEndpoint='${commonContext.apiEndpoint}']",
        )
    }
}
