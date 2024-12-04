import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

object LocaleScanner {

    /**
     * Scans for available translations and returns supported language tags
     */
    @JvmStatic
    fun getAvailableLanguageTags(project: Project): String {
        val translationsFile = try {
            File(project.projectDir, "../../translations/projects.json").canonicalFile
        } catch (e: IOException) {
            println("\nUnable to find translation project config file (translations/projects.json)")
            println("Exiting...")
            Thread.sleep(1000) // Allow time to print message
            exitProcess(1)
        }
        val root = translationsFile.parentFile.parentFile
        val translationsJson = translationsFile.readText()
        val type = object : TypeToken<List<TranslationProject>>() {}.type
        val projects = Gson().fromJson<List<TranslationProject>>(translationsJson, type)
        val resDirs = projects.map { it.resourceDir }
        val resNames = resDirs.asSequence()
            .map { File(root, it) }
            .flatMap { dir ->
                dir.walkTopDown()
                    .filter { it.name == "strings.xml" && it.parentFile.name != "values" }
                    .map { it.parentFile }
            }
            .map { dir ->
                val name = dir.name.substringAfter("values-").replace("-r", "-")
                name.takeUnless { it.startsWith("b+") } ?: name.drop(2).replace("+", "-")
            }
            .distinct()
            .sorted()
            .toList()
        println("    :LocaleScanner found ${resNames.size} translations")
        return resNames.toTypedArray().joinToString(";")
    }
}

private data class TranslationProject(
    val name: String,
    @SerializedName("source_path")
    val sourcePath: String,
    @SerializedName("resource_dir")
    val resourceDir: String
)
