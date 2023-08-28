# CommandK CLI
The CommandK CLI is a command line interface for interacting with the CommandK API. The CLI is currently available for Linux, and macOS.

- [CommandK CLI](#commandk-cli)
  * [Download and Install](#download-and-install)
    + [Homebrew (MacOS)](#homebrew--macos)
    + [Linux (or MacOS without brew)](#linux--or-macos-without-brew)
  * [Setup](#setup)
- [Operations](#operations)
  * [Getting Secrets](#getting-secrets)
  * [Running Commands with Secrets](#running-commands-with-secrets)
    + [Running with environment variables](#running-with-environment-variables)
    + [Running with storing to a file](#running-with-storing-to-a-file)
  * [Importing Secrets](#importing-secrets)
  * [Getting help with options and arguments](#getting-help-with-options-and-arguments)
  * [Application names and identifiers](#application-names-and-identifiers)
    + [Disambiguating with application names](#disambiguating-with-application-names)
- [Working with frameworks](#working-with-frameworks)
  * [Next.js](#nextjs)
  * [NestJS](#nestjs)
  * [Nuxt](#nuxt)
  * [Gatsby](#gatsby)
  * [Remix](#remix)
  * [Vite](#vite)
  * [Django](#django)
  * [Flask](#flask)
  * [Laravel](#laravel)
  * [.NET](#net)
  * [Spring boot](#spring-boot)
- [Configuring the CLI](#configuring-the-cli)
- [Auditing CLI Access](#auditing-cli-access)

## Download and Install

### Homebrew (MacOS)

To install the commandk cli via `homebrew`, use the CommandK homebrew tap

```shell
$ brew tap commandk-dev/packages
$ brew install cmdk-cli
```

If the installation went through smoothly, you should be able to run:

```shell
$ cmdk version
```

### Linux (or MacOS without brew)

1. The CommandK CLI can be downloaded from the [releases page](https://github.com/commandk-dev/cli/releases)
2. After downloading the ZIP file, extract its contents using the following command (replace <file> with the actual downloaded file name), and install the binary:
    
    ```shell
    $ unzip /path/to/archive.zip
    $ # You might have to use sudo for the next command
    $ install <archive>/cmdk-linux-x86_64 -m 0555 /usr/local/bin/cmdk
    ```
If the installion went through successfully, you should be able to run:

```shell
$ cmdk version
```

## Setup

To begin using the CLI, you'll first need to get an access token. To do that, head over to the CommandK dashboard, and
on the left panel, click on Settings > API Access. In the tab that opens up, in the top-right corner, click on the
button that says "New token". Enter a memorable name for your API token, select the applications that you want this
token to be able to access to, and the environments that you want to restrict this action to. You can also select the
scope of the token to be either `Read` or `Write`. Do note that, `Write` is not a superset of a `Read` token, and a
`Write` token does not allow any `Read` operations.

> **TODO** Add a loom/screenshot of the API Access Token Page

Click on "Create" and copy the access token that was generated. Do note that we do not display the access token once
this panel is collapsed, so make sure you copy the token before dismissing the panel.

Create a new file named `.commandk.config.json` in your home directory with the following data:

```json
{
    "api-access-token": "... the token you just created ..."
}
```

> **NOTE** You MUST be an Admin or an Owner to be able to create API Access tokens

> **NOTE** If you wish to use application names while using the CLI, then you must select the "all" apps option.
> If you are using tokens that are restricted to certain apps, then you must use app identifiers when using the CLI
> Check the section on [Application names and identifiers](#application-names-and-identifiers) for details on how
> to do this.

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

To import secrets, get the app name from the dashboard you want to import secrets for. Then create a file, with any name,
for ex. `secrets-staging.json` with the following structure:

```json
{
  "database_password": "<your-db-passworD>",
  "api_key": "<your-api-key>",
  "encryption_key": "<your-encryption-key>"
}
```

To use this file to import secrets to an app, use the `cmdk secrets import` command:

```shell
$ cmdk secrets import return-service-management \
     --environment staging \
     --import-file secrets-staging.json
✅ Using environment Development [id: yueOX-iOZN-UapbH-ruAP]
Read [3] secrets to upload
Using application with UUID [6L4Wd-oY7z-eLCFv-qc8R]
Wrote secret [database_password]
Wrote secret [api_key]
Wrote secret [encryption_key]
```

> **NOTE** You will need to use an API access token with `Write` scope to be able to run this command.

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
All CLI actions invariably operate on a particular app within CommandK. For ease of use, by default the CLI always accepts
application names, but in environments where you would like to use something more predictable and robust, you can specify
app identifiers as well. Whenever using an application identifier instead of an app name, simply specify the
flag `--identifier-type Identifier` (this flag defaults to `Name`). For example:

```shell
$ cmdk secrets get LDJML-6x65-saP5d-7jBL \
     --identifier-type Identifier \
     --environment staging \
     --output-file-name secrets.env
```

The app id can be fetched from the side panel under the "Settings" tab, for a given app:

![image](https://github.com/commandk-dev/cli/assets/136263/f88f0533-e98d-4478-82ec-17d9ffebc0c8)

### Disambiguating with application names
To identify multiple artifacts of the same app, CommandK allows the creation of apps with the same name, as long as the "app-type"
of the app is different. If you are operating on such an app, you might see the error message:

```
The provided name for the application was not found to be unique, please specify --app-type to narrow it down
```

To disambiguate this, simply specify the app-type of app you are trying to access:

```
$ cmdk secrets get return-management-service \
     --app-type CronJob
     --environment staging \
     --output-file-name secrets.env
```

The Type of the app can be viewed under the `Settings` tab.

# Working with frameworks

## Next.js

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run dev
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run dev
```

## NestJS

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run start:dev
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run start:dev
```

## Nuxt

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run dev
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run dev
```

## Gatsby

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run develop
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run develop
```

## Remix

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run dev
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run dev
```

## Vite

Simply use the CommandK CLI to execute your usual run command

```shell
$ cmdk run <application-name> --environment development -- npm run dev
```

If you are using `dotenv`, you can even have it write to an env file and then run your service:

```shell
$ cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name .env.local \
     --file-format env \
     -- npm run dev
```

## Django

Simply use the CommandK CLI to execute your usual run command, an example for python is as shown below:

```shell
$ cmdk run <application-name> --environment development -- python server.py server
```

## Flask

Simply use the CommandK CLI to execute your usual run command, an example for python is as shown below:

```shell
$ cmdk run <application-name> --environment development -- python server.py server
```

## Laravel

Simply use the CommandK CLI to execute your usual run command, an example for PHP is as shown below:

```shell
$ cmdk run <application-name> --environment development -- php artisan serve
```

## .NET

Simply use the CommandK CLI to execute your usual run command, an example for .NET is as shown below:

```shell
$ cmdk run <application-name> --environment development -- dotnet run
```

## Spring boot

Simply use the CommandK CLI to execute your usual run command. An example would be:

```shell
$ cmdk run <application-name> --environment development -- ./gradlew bootRun
```

or if you are using Maven,

```shell
$ cmdk run <application-name> --environment development -- ./mvnw spring-boot:run
```

You could also leverage spring profiles to load secrets from a YAML file seamlessly:

```shell
$ SPRING_PROFILES_ACTIVE=my-secrets cmdk run <application-name> --environment development \
     --run-type file-store \
     --file-name config/application-my-secrets.yaml \
     --file-format yaml \
     -- ./gradlew bootRun
```

In the above example, we are storing secrets in a file called `application-my-secrets.yaml` and activating
that profile when running spring. Depending on how your application handles profiles, the setup might have
to change accordingly to accommodate additional profiles.

# Configuring the CLI

The common arguments to a CLI can be configured via (in order of precedence):
1. Specifying config parameters as CLI arguments
2. Specifying the parameters in an environment file
3. Specifying the parameters in a configuration file

By default, the configuration file that is loaded is `.commandk.config.json` located in the user's home directory.
This can be overridden by specifying the environment variable `CMDK_CONFIG_FILE`.

The following lists the common parameters for the CLI that can be configured:

| CLI Argument       | Environment Variable     | Config file attribute | Default Value                 | Description                                                        |
|--------------------|--------------------------|-----------------------|-------------------------------|--------------------------------------------------------------------|
| --api-access-token | CMDK_API_ACCESS_TOKEN    | api-access-token      |                               | The API access token used by the CLI                               |
| --api-endpoint     | CMDK_API_ENDPOINT        | api-endpoint          | https://api.commandk.dev      | The API endpoint that the CLI performs the operations against      |
| --environment      | CMDK_DEFAULT_ENVIRONMENT |                       |                               | Environment to be used if not specified via the --environment flag |
|                    | CMDK_CONFIG_FILE         |                       | `$HOME/.commandk.config.json` | Specify an alternate location for the commandk configuration file  |

# Auditing CLI Access

All activities performed by the CLI are logged under the activity log, with the acting entity being the access token that was used by the CLI. To access the log, simply click on "Activity Log" on the left panel. If there have been recent actions performed via the CLI, their tokens would show up with the actions they performed and the entities the actions operated over:

![Screenshot_20230827_151421](https://github.com/commandk-dev/cli/assets/136263/df99a4b9-d5d7-4f4e-8d5b-ce58318ed327)

Click on any row item to get further details about the logged event.
