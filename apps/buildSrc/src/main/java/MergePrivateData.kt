@file:Suppress("unused")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.util.*

object PrivateData {

    private const val PRINT_PAD_SIZE = 60

    @JvmStatic
    @JvmOverloads
    fun merge(project: Project, dataDirName: String = "") {

        val baseDir = resolvePrivateDataDir(project.projectDir)
        val dataDir = File(baseDir, dataDirName).canonicalFile

        println("")
        println("============= MERGE PRIVATE FILES: ${dataDirName.toUpperCase()} ".padEnd(PRINT_PAD_SIZE, '='))

        /* Confirm dir exists */
        if (!dataDir.exists() || !dataDir.isDirectory) {
            failMerge("Unable to find private data source directory at ${dataDir.canonicalPath}. Please add private project files.")
        }

        /* Confirm files list exists */
        val fileList = File(dataDir, "files.list")
        if (!fileList.exists()) {
            failMerge("Unable to find file list at ${fileList.canonicalPath}. Ensure private project files have been added.")
        }

        /* Grab list of files */
        val props = Properties()
        FileInputStream(fileList).use { props.load(it) }

        /* Copy files and properties */
        props.stringPropertyNames().forEach { srcName ->
            val src = File(dataDir, srcName)
            if (!src.exists() || !src.isFile) {
                failMerge("Could not find source file at ${src.canonicalPath}")
            }

            /* Merge private.properties */
            if (srcName == "private.properties") {
                println("Merging private.properties:")
                val privateProps = Properties()
                FileInputStream(src).use { privateProps.load(it) }
                privateProps.stringPropertyNames().forEach { propName ->
                    println("    $propName")
                    var value = privateProps.getProperty(propName)
                    if (value.startsWith("file:")) {
                        val fileName = value.replaceFirst("file:", "")
                        val file = File(dataDir, fileName)
                        if (!file.exists()) {
                            failMerge("Could not find source file ${file.canonicalPath} for '$propName' specified in private.properties")
                        }
                        value = file.readText()
                    }
                    val escaped = value.replace("\"", "\\\"").replace("[\n\r]", "")
                    project.extra.set(propName, escaped)
                }
            } else {
                val dstPath = props.getProperty(srcName)
                val dst = File(project.projectDir, dstPath)

                /* Make parent dir if necessary */
                val dstParent = dst.parentFile
                if (!dstParent.exists()) dstParent.mkdirs()

                if (!dst.exists()) {
                    println("Copying ${src.canonicalPath} to $dst")
                    src.copyTo(dst, true)
                } else if (!src.md5.contentEquals(dst.md5)) {
                    println("${dst.canonicalPath} differs from $src and will be replaced")
                    src.copyTo(dst, true)
                } else {
                    println("${dst.canonicalPath} exists and is UP-TO-DATE")
                }
            }
        }

        println("".padEnd(PRINT_PAD_SIZE, '='))
        println("")
    }

    private fun failMerge(message: String): Nothing = throw Exception("Failed to merge private data. $message")

    private val File.md5 get() = MessageDigest.getInstance("MD5").digest(readBytes())

    private tailrec fun resolvePrivateDataDir(srcDir: File?): File {
        if (srcDir == null) throw FileNotFoundException("Could not locate private data directory!")
        return srcDir.resolve("private-data").takeIf { it.exists() && it.isDirectory }
                ?: resolvePrivateDataDir(srcDir.parentFile)
    }
}
