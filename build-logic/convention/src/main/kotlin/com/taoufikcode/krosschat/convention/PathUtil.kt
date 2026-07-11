package com.taoufikcode.krosschat.convention

import org.gradle.api.Project
import java.util.Locale

fun Project.pathToPackageName(): String {
    val relativePackageName = path.replace(
        ":", "."
    ).lowercase()
    return "com.taoufikcode$relativePackageName"
}

fun Project.pathToFrameworkName(): String {
    val parts = this.path.split(":", "-", "_").filter { it.isNotEmpty() }
    val result = parts.joinToString("") { part ->
        part.replaceFirstChar {
            it.titlecase(Locale.ROOT)
        }
    }
    return result
}