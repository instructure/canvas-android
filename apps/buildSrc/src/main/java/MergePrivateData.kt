@file:Suppress("unused")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.util.Base64

/**
 * Loads private build data (PSPDFKit license, OAuth secrets, google-services.json, etc.)
 * from environment variables. Required env vars must be set by:
 *   - CI: workflow `env:` blocks wire `${{ secrets.X }}` -> env var X
 *   - Local dev: `source scripts/setup-private-env.local.sh` before invoking gradle
 *
 * No on-disk staging, no fallback. Missing required values fail the build with a pointer
 * to scripts/setup-private-env.sh. See docs/private-data-migration-plan.md.
 */
object PrivateData {

    private const val PRINT_PAD_SIZE = 60

    private data class PropMapping(val section: String, val propName: String, val envVar: String)

    /** [destRelPath] is relative to project.projectDir of the calling module. */
    private data class FileMapping(val section: String, val destRelPath: String, val envVar: String)

    private val PROPERTY_MAPPINGS = listOf(
        PropMapping("all-apps", "pspdfMavenUser", "PSPDF_MAVEN_USER"),
        PropMapping("all-apps", "pspdfMavenPass", "PSPDF_MAVEN_PASS"),
        PropMapping("annotations", "pspdfMavenUser", "PSPDF_MAVEN_USER"),
        PropMapping("annotations", "pspdfMavenPass", "PSPDF_MAVEN_PASS"),
        PropMapping("login-api", "anonymousSchoolDomain", "LOGIN_ANONYMOUS_SCHOOL_DOMAIN"),
        PropMapping("parent", "loginClientId", "PARENT_LOGIN_CLIENT_ID"),
        PropMapping("parent", "loginClientSecret", "PARENT_LOGIN_CLIENT_SECRET"),
        PropMapping("parent", "pendoAccessToken", "PENDO_ACCESS_TOKEN_PARENT"),
        PropMapping("parent", "roboTestApiKey", "ROBO_TEST_API_KEY"),
        PropMapping("parent", "roboTestUserId", "ROBO_TEST_USER_ID"),
        PropMapping("parent", "roboTestUsername", "ROBO_TEST_USERNAME"),
        PropMapping("student", "pspdfkitLicenseKey", "PSPDFKIT_LICENSE_KEY_STUDENT"),
        PropMapping("student", "pspdfMavenUser", "PSPDF_MAVEN_USER"),
        PropMapping("student", "pspdfMavenPass", "PSPDF_MAVEN_PASS"),
        PropMapping("student", "pendoAccessToken", "PENDO_ACCESS_TOKEN_STUDENT"),
        PropMapping("student", "pronounTestStudent", "PRONOUN_TEST_STUDENT"),
        PropMapping("student", "pronounTestStudentPassword", "PRONOUN_TEST_STUDENT_PASSWORD"),
        PropMapping("student", "pushNotificationsTestStudent", "PUSH_NOTIF_TEST_STUDENT"),
        PropMapping("student", "pushNotificationsTestStudentPassword", "PUSH_NOTIF_TEST_STUDENT_PASSWORD"),
        PropMapping("teacher", "pspdfMavenUser", "PSPDF_MAVEN_USER"),
        PropMapping("teacher", "pspdfMavenPass", "PSPDF_MAVEN_PASS"),
        PropMapping("teacher", "pspdfkitLicenseKey", "PSPDFKIT_LICENSE_KEY_TEACHER"),
        PropMapping("teacher", "pendoAccessToken", "PENDO_ACCESS_TOKEN_TEACHER"),
        PropMapping("teacher", "roboTestUserToken", "ROBO_TEST_USER_TOKEN"),
        PropMapping("teacher", "roboTestDomain", "ROBO_TEST_DOMAIN"),
        PropMapping("teacher", "pronounTestTeacher", "PRONOUN_TEST_TEACHER"),
        PropMapping("teacher", "pronounTestTeacherPassword", "PRONOUN_TEST_TEACHER_PASSWORD"),
        PropMapping("teacher", "pushNotificationsTestTeacher", "PUSH_NOTIF_TEST_TEACHER"),
        PropMapping("teacher", "pushNotificationsTestTeacherPassword", "PUSH_NOTIF_TEST_TEACHER_PASSWORD"),
    )

    private val FILE_MAPPINGS = listOf(
        FileMapping("parent", "google-services.json", "GOOGLE_SERVICES_PARENT_B64"),
        FileMapping("student", "google-services.json", "GOOGLE_SERVICES_STUDENT_B64"),
        FileMapping("student", "src/main/res/xml/analytics.xml", "ANALYTICS_XML_STUDENT_B64"),
        FileMapping("student", "src/main/res/values/private_strings.xml", "PRIVATE_STRINGS_XML_STUDENT_B64"),
        FileMapping("teacher", "google-services.json", "GOOGLE_SERVICES_TEACHER_B64"),
        FileMapping("teacher", "robo_user.json", "ROBO_USER_JSON_TEACHER_B64"),
        FileMapping("dataseeding", "src/main/java/com/instructure/dataseeding/util/PrivateConst.kt", "PRIVATE_CONST_KT_B64"),
    )

    @JvmStatic
    @JvmOverloads
    fun merge(project: Project, dataDirName: String = "") {
        val propMappings = PROPERTY_MAPPINGS.filter { it.section == dataDirName }
        val fileMappings = FILE_MAPPINGS.filter { it.section == dataDirName }

        if (propMappings.isEmpty() && fileMappings.isEmpty()) {
            // Section has nothing wired (e.g. vestigial). Skip silently.
            return
        }

        val shellEnv = shellEnvFor(project)

        println("")
        println("============= MERGE PRIVATE DATA: ${dataDirName.uppercase()} ".padEnd(PRINT_PAD_SIZE, '='))

        if (propMappings.isNotEmpty()) {
            println("Properties:")
            propMappings.forEach { mapping ->
                val value = resolveEnvVar(mapping.envVar, shellEnv)
                    ?: failMerge("Required env var ${mapping.envVar} is not set (section: ${mapping.section}, property: ${mapping.propName}). Add it to scripts/setup-private-env.local.sh or the workflow env block.")
                println("    ${mapping.propName}  <-  ${mapping.envVar}")
                val escaped = value.replace("\"", "\\\"").replace("[\n\r]", "")
                project.extra.set(mapping.propName, escaped)
            }
        }

        fileMappings.forEach { mapping ->
            val envVal = resolveEnvVar(mapping.envVar, shellEnv)
                ?: failMerge("Required env var ${mapping.envVar} is not set (section: ${mapping.section}, file: ${mapping.destRelPath}).")
            val bytes = Base64.getDecoder().decode(envVal.replace("\\s".toRegex(), ""))
            writeIfChanged(File(project.projectDir, mapping.destRelPath), bytes, mapping.envVar)
        }

        println("".padEnd(PRINT_PAD_SIZE, '='))
        println("")
    }

    private fun resolveEnvVar(name: String, shellEnv: Map<String, String>): String? {
        val fromProcess = System.getenv(name)
        if (!fromProcess.isNullOrBlank()) return fromProcess
        return shellEnv[name]?.takeIf { it.isNotBlank() }
    }

    /** Lazily parsed once per Gradle invocation (synchronized on first call). */
    @Volatile
    private var cachedShellEnv: Map<String, String>? = null

    @Synchronized
    private fun shellEnvFor(project: Project): Map<String, String> {
        cachedShellEnv?.let { return it }
        val repoRoot = resolveRepoRoot(project.projectDir)
        val localFile = File(repoRoot, "scripts/setup-private-env.local.sh")
        val templateFile = File(repoRoot, "scripts/setup-private-env.sh")
        val source = when {
            localFile.isFile -> localFile
            templateFile.isFile -> templateFile
            else -> null
        }
        val env = source?.let { parseShellExports(it) } ?: emptyMap()
        if (source != null) {
            println("PRIVATE_DATA: pre-flight loaded ${env.size} env vars from ${source.name}: ${env.keys.joinToString(", ")}")
        } else {
            println("PRIVATE_DATA: no setup-private-env.{local.,}sh found at repo root; relying on process env only")
        }
        cachedShellEnv = env
        return env
    }

    /**
     * Parse simple `export NAME="value"` / `export NAME='value'` / `export NAME=value` lines.
     * Comments (#...) and empty lines ignored. Anything more complex (command substitution,
     * variable expansion, multi-line values) is ignored — the template only uses simple exports.
     */
    private fun parseShellExports(file: File): Map<String, String> {
        val nameRegex = Regex("^[A-Z_][A-Z0-9_]*$")
        val out = LinkedHashMap<String, String>()
        var skippedUnparseable = 0
        file.readLines().forEachIndexed { idx, rawLine ->
            var line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed
            if (!line.startsWith("export ") && !line.startsWith("export\t")) return@forEachIndexed
            line = line.removePrefix("export").trimStart()
            val eqIdx = line.indexOf('=')
            if (eqIdx <= 0) {
                skippedUnparseable++
                println("PRIVATE_DATA: skipping ${file.name}:${idx + 1} — no `=`")
                return@forEachIndexed
            }
            val name = line.substring(0, eqIdx).trim()
            if (!name.matches(nameRegex)) {
                skippedUnparseable++
                println("PRIVATE_DATA: skipping ${file.name}:${idx + 1} — name '$name' fails [A-Z_][A-Z0-9_]* check")
                return@forEachIndexed
            }
            val rest = line.substring(eqIdx + 1)
            val value = when {
                rest.startsWith("\"") -> {
                    val end = rest.indexOf('"', startIndex = 1)
                    if (end < 0) {
                        skippedUnparseable++
                        println("PRIVATE_DATA: skipping ${file.name}:${idx + 1} ($name) — unterminated double quote")
                        return@forEachIndexed
                    }
                    rest.substring(1, end)
                }
                rest.startsWith("'") -> {
                    val end = rest.indexOf('\'', startIndex = 1)
                    if (end < 0) {
                        skippedUnparseable++
                        println("PRIVATE_DATA: skipping ${file.name}:${idx + 1} ($name) — unterminated single quote")
                        return@forEachIndexed
                    }
                    rest.substring(1, end)
                }
                else -> rest.substringBefore('#').trim()
            }
            out[name] = value
        }
        if (skippedUnparseable > 0) {
            println("PRIVATE_DATA: $skippedUnparseable line(s) in ${file.name} were not parsed")
        }
        return out
    }

    /** Walk up from projectDir until we find scripts/setup-private-env.sh — the canonical repo root anchor. */
    private tailrec fun resolveRepoRoot(srcDir: File?): File {
        if (srcDir == null) throw FileNotFoundException(
            "Could not locate repo root (walked up from project.projectDir without finding scripts/setup-private-env.sh)."
        )
        return srcDir.takeIf { File(it, "scripts/setup-private-env.sh").isFile }
                ?: resolveRepoRoot(srcDir.parentFile)
    }

    private fun writeIfChanged(dst: File, bytes: ByteArray, sourceEnvVar: String) {
        val dstParent = dst.parentFile
        if (dstParent != null && !dstParent.exists()) dstParent.mkdirs()

        if (!dst.exists()) {
            println("Writing ${dst.canonicalPath}  <-  $sourceEnvVar")
            dst.writeBytes(bytes)
            return
        }
        val incomingMd5 = MessageDigest.getInstance("MD5").digest(bytes)
        val currentMd5 = MessageDigest.getInstance("MD5").digest(dst.readBytes())
        if (!incomingMd5.contentEquals(currentMd5)) {
            println("Replacing ${dst.canonicalPath}  <-  $sourceEnvVar")
            dst.writeBytes(bytes)
        } else {
            println("${dst.canonicalPath} UP-TO-DATE")
        }
    }

    private fun failMerge(message: String): Nothing =
        throw Exception("Failed to merge private data. $message")
}
