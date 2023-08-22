package dev.commandk.cli.helpers

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.io.IOException
import platform.posix.E2BIG
import platform.posix.EACCES
import platform.posix.EFAULT
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ELOOP
import platform.posix.EMFILE
import platform.posix.ENAMETOOLONG
import platform.posix.ENFILE
import platform.posix.ENOENT
import platform.posix.ENOEXEC
import platform.posix.ENOMEM
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.ETXTBSY
import platform.posix.errno

class RunUtils {
    @OptIn(ExperimentalForeignApi::class)
    fun run(commands: List<String?>, env: List<String?>) {
        memScoped {
            platform.posix.execve(
                commands[0],
                allocArrayOf(commands.map { it?.cstr?.getPointer(memScope) }),
                allocArrayOf(env.map { it?.cstr?.getPointer(memScope) })
            )

            val x = when (errno) {
                E2BIG -> IOException("The total number of bytes in the environment (envp) and argument list (argv) is too large.")
                EACCES -> IOException("The file or a script interpreter is not a regular file.\nOr Execute permission is denied for the file or a script or ELF interpreter.\nOr The file system is mounted noexec.")
                EFAULT -> IOException("filename points outside your accessible address space.")
                EINVAL -> IOException("An ELF executable had more than one PT_INTERP segment (i.e., tried to name more than one interpreter).")
                EIO -> IOException("An I/O error occurred.")
                EISDIR -> IOException("An ELF interpreter was a directory.")
                ELOOP -> IOException("Too many symbolic links were encountered in resolving filename or the name of a script or ELF interpreter.")
                EMFILE -> IOException("The process has the maximum number of files open.")
                ENAMETOOLONG -> IOException("filename is too long.")
                ENFILE -> IOException("The system limit on the total number of open files has been reached.")
                ENOENT -> IOException("The file filename or a script or ELF interpreter does not exist, or a shared library needed for file or interpreter cannot be found.")
                ENOEXEC -> IOException("An executable is not in a recognized format, is for the wrong architecture, or has some other format error that means it cannot be executed.")
                ENOMEM -> IOException("Insufficient kernel memory was available.")
                ENOTDIR -> IOException("A component of the path prefix of filename or a script or ELF interpreter is not a directory.")
                EPERM -> IOException("The file system is mounted nosuid, the user is not the superuser, and the file has the set-user-ID or set-group-ID bit set.")
                ETXTBSY -> IOException("Executable was open for writing by one or more processes.")

                else -> Unit
            }

            if (x is Exception) {
                throw x
            }
        }
    }
}
