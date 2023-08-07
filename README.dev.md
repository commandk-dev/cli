# CommandK CLI

## Development
To run the CLI locally, run the following command:

```shell
./gradlew runDebugExecutableNative -PrunArgs='<cli-args>'
```

If you want to run the test command for testing purposes, run the following command:

```shell
CMDK_ENABLE_TEST_COMMAND=true ./gradlew runDebugExecutableNative -PrunArgs='<cli-args>'
```

## Configuration
The CLI properties can be configured via three methods:
1. Specifying the arguments via command line parameters
2. Specifying the arguments via environment variables
3. Specifying the arguments via a configuration file

For example, to specify the "api-access-token", you can run the command in the following three ways:

```shell
cmdk --api-access-token=<api-access-token> <command>
```

or, to specify environment variables

```shell
CMDK_API_ACCESS_TOKEN=<api-access-token> cmdk <command>
```

or, to specify a configuration file, create a file `~/.commandk.config.json` with the following entry:

```json
{
  "api-access-token": "<api-access-tokenL"
}
```

and then run,

```shell
cmdk <command>
```

The file location is `~/.commandk.config.json` by default, but can be overridden by specifying the environment variable 
`CMDK_CONFIG_FILE`.

## Convention

1. All environment variables used by the CommandK cli MUST start with `CMDK_`