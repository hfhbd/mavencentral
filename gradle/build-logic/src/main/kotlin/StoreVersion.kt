import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

@CacheableTask
abstract class StoreVersion : DefaultTask() {
    @get:Input
    abstract val version: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        outputDirectory.convention(project.layout.buildDirectory.dir("generated/version"))
    }

    @TaskAction
    fun action() {
        File(outputDirectory.get().asFile, "Version.kt").writeText(
            version.get().entries.joinToString(
                prefix = "package io.github.hfhbd.mavencentral.gradle\n",
                separator = "\n",
                postfix = "\n",
            ) { (name, gav) ->
                "public const val $name = \"$gav\""
            }
        )
    }
}
