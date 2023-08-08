# CommandK CLI
The CommandK CLI is a command line interface for interacting with the CommandK API. It is written in Kotlin and compiled
to a native executable. The CLI is available for Linux, and macOS.

## Download and Install

1. The CommandK CLI can be downloaded from the [releases page](https://github.com/commandk-dev/cli/releases)
2. After downloading the ZIP file, extract its contents using the following command (replace <file> with the actual downloaded file name):
    
    ```shell
    tar -xvf /path/to/archive.tar.gz -C /path/to/directory
    ```
   
3. Create a symlink to the `cmdk` executable in the `bin` directory of the extracted contents:

    ```shell
    sudo ln -s /path/to/directory/cmdk /usr/local/bin/cmdk
    ```