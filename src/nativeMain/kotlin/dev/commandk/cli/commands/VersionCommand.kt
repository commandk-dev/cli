package dev.commandk.cli.commands

import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import dev.commandk.cli.CommandK
import dev.commandk.cli.context.CommonContext
import dev.commandk.cli.context.cc
import dev.commandk.cli.context.executeCliCommand

class VersionCommand : CliktCommand("Get the current CommandK CLI version") {
    private val commonContext by requireObject<CommonContext>()
    override fun run() {
        commonContext.executeCliCommand {
            cc().writeLine("CommandK / CLI")
            cc().writeLine("  Version - ${CommandK.Version}")
            Unit.right()
        }
    }
}
