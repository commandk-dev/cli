# CommandK CLI
The CommandK CLI is a command line interface for interacting with the CommandK API. It is written in Kotlin and compiled
to a native executable. The CLI is available for Linux, and macOS.

## Download and Install

1. The CommandK CLI can be downloaded from the [releases page](https://github.com/commandk-dev/cli/releases)
2. After downloading the ZIP file, extract its contents using the following command (replace <file> with the actual downloaded file name):
    
    ```shell
    $ tar -xvf /path/to/archive.tar.gz -C /path/to/directory
    ```
   
3. Create a symlink to the `cmdk` executable in the `bin` directory of the extracted contents:

    ```shell
    $ sudo ln -s /path/to/directory/cmdk /usr/local/bin/cmdk
    ```

## Setup

To begin using the CLI, you'll first need to get an access token. To do that, head over to the CommandK dashboard, and
on the left panel, click on Settings > API Access. In the tab that opens up, in the top-right corner, click on the
button that says "New token". Enter a memorable name for your API token, select the applications that you want this
token to be able to access to, and the environments that you want to restrict this action to. You can also select the
scope of the token to be either `Read` or `Write`. Do note that, `Write` is not a superset of a `Read` token, and a
`Write` token does not allow any `Read` operations.

Click on "Create" and copy the access token that was generated. Do note that we do not display the access token once
this panel is collapsed, so make sure you copy the token before dismissing the panel.

Create a new file named `.commandk.config.json` in your home directory with the following data:

```json
{
    "api-access-token": "... the token you just created ..."
}
```

Now you are all setup to start using the CommandK CLI!

> **NOTE** If you are using an on-prem installation of the CLI, you'll have to specify an alternate endpoint in your
> `.commandk.config.json` file. Your file would look like:
> 
> ```json
> {
>    "api-access-token": "<your-access-token>",
>    "api-endpoint": "https://api.<installation-name>.commandk.dev"
> }
>```
> For the value of `api-endpoint`, refer to the Customer Information Sheet that would have been shared by the CommandK 
> team for your installation. Usually, if you access your dashboard at `app.<name>.commandk.dev`, then the API endpoint
> would be `https://api.<name>.commandk.dev`

# Operations

## Getting Secrets

To get secrets for an application, you can use the `cmdk secrets get` command. It follows the format:

```shell
$ cmdk secrets get <application-name> --environment <environment-name> --output-file-name <file-name>
```

For example,

```shell
$ cmdk secrets get return-management-service \
     --environment staging \
     --output-file-name my-secrets.env
Writing fetched secrets to file `my-secrets.env`
```

By default, secrets are always written in the "env" format, where each key/value pair is listed on a line, with they
key and value being seperated by a `=` sign. The CLI also supports output in `json` and `yaml` format, which you can
specify using the `--output` flag. For example,

```shell
$ cmdk secrets get return-management-service \
     --environment staging \
     --output-file-name my-secrets.json \
     --output json
```

Owing to the sensitive nature of secrets, the CLI does not support writing secrets to stdout. If you wish to pipe
values to a script, you can use a temp file in your script.

## Running Commands with Secrets

In addition to writing secrets to a file, you can also run a command with the secrets being injected to environment
variables for the command, or alternatively, to write to a file and then execute a command - the latter being a
convenience mechanism that gives you a single command to fetch secrets and run your applications.

### Running with environment variables

To execute a process with fetched secrets as environment variables, you can use the command `cmdk run`:

```shell
$ cmdk run <application-name> --environment <environment-name> -- <command>
```

For example,

```shell
$ cmdk run return-management-service --environment development -- node index.js
```

This command will populate the secrets for `return-management-service` within the environment `development` and present
them as environment variables to the command.

### Running with storing to a file

To fetch secrets to a file and then execute a command, you can use the `--run-type file-store` flag. For example,

```shell
$ cmdk run return-management-service \
      --environment development \
      --run-type file-store \
      --file-name env.json \
      --file-format json \
      -- node index.js
```

The arguments `--file-name` and `--file-format` specify the name of the file and the format the secrets are written in
before executing the specified command.

## Importing Secrets

## Getting help with options and arguments
All subcommands are documented and can be listed by passing in the `--help` flag. To get a list of the base commands,
simply run

```shell
$ cmdk --help
Usage: cmdk [<options>] <command> [<args>]...

  cmdk

Options:
  --api-access-token=<text>  The API Access Token used to make API calls
  --api-endpoint=<text>      The HTTPs endpoint for the CommandK API Server
  -h, --help                 Show this message and exit

Commands:
  secrets  Perform operations on secrets via the CommandK CLI
  run      Fetch secrets, and invoke the application runner
```

To get more information about a specific subcommand, simply run that command with the `--help` flag, for example, to
get more information about the `secrets import` command, or the `run` command execute:

```shell
$ cmdk secrets import --help
```

```shell
$ cmdk run --help
```

## Application names and identifiers
You can use the CLI to 

# Working with frameworks

## Next.js

Simply use the 

## Spring

# Configuring the CLI

## Modes of configuration

## Table of parameters

# Auditing CLI Access
