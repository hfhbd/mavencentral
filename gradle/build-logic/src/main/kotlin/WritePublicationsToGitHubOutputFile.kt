import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

@CacheableTask
abstract class WritePublicationsToGitHubOutputFile : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val publicationFiles: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootDirectory: DirectoryProperty

    @get:OutputFile
    abstract val githubOutputFile: RegularFileProperty

    @TaskAction
    internal fun action() {
        githubOutputFile.get().asFile.appendText(
            publicationFiles.joinToString(
                prefix = "publishedFiles<<EOF\n",
                separator = "\n",
                postfix = "\nEOF\n",
            ) {
                require(it.exists()) { "File does not exist: ${it.absolutePath}" }
                it.toRelativeString(rootDirectory.get().asFile)
            }
        )
    }
}
