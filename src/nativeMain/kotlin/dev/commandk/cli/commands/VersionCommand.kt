package dev.commandk.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.terminal.Terminal
import dev.commandk.cli.CommandK

class VersionCommand(
    private val terminal: Terminal
) : CliktCommand("Get the current CommandK CLI version") {
    override fun run() {
        terminal.println("CommandK / CLI")
        terminal.println("  Version - ${CommandK.Version}")
    }
}
