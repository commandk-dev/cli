package dev.commandk.cli.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice

sealed class RunType(
    val name: String
) : OptionGroup(name)

class FileStoreRunType : RunType("file-store") {
    val fileFormat by option(
        "--file-format",
        help = "The format of data to store in the file",
    ).choice("json", "yaml", "env")
        .required()

    val fileName by option(
        "--file-name",
        help = "The location of the file to read from",
    ).required()
}

class EnvVarRunType : RunType("env-var")

