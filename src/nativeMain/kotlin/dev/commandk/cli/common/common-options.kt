package dev.commandk.cli.common

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice

val environmentOption: ParameterHolder.() -> OptionWithValues<String, String, String> = {
    option(
        "--environment",
        help = "The environment to import the secrets to",
        envvar = CommonEnvironmentVars.DefaultEnvironment,
    ).required()
}

val subTypeOption: ParameterHolder.() -> NullableOption<String, String> = {
    option(
        "--sub-type",
        help = "Disambiguation for application name, if multiple applications with the same name exists",
    )
        .choice("Service", "CronJob", "Job", "Other")
}

val identifierTypeOption: ParameterHolder.() -> OptionWithValues<String, String, String> = {
    option(
        "--identifier-type",
        help = "The type of identifier being specified for an application (defaults to 'Name')",
    )
        .choice("Identifier", "Name")
        .default("Name")
}

val outputFormatOption: ParameterHolder.() -> OptionWithValues<String, String, String> = {
    option(
        names = arrayOf("--output"),
        help = "The output format to use",
    ).choice("json", "yaml", "env")
        .default("env")
}

val applicationNameOption: ParameterHolder.() -> OptionWithValues<String, String, String> = {
    option(
        "--application-name",
        help = "The application name to fetch the secrets for",
    ).required()
}

fun CliktCommand.applicationNameArgument(
    help: String = "The name of the CommandK application"
) : ProcessedArgument<String, String> =
    argument(
        name = "application-name",
        help = help
    )
