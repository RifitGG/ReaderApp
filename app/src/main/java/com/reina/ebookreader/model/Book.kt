package com.reina.ebookreader.model

import android.net.Uri
import java.io.File
import java.util.UUID

data class Book(
    val id: String,
    val title: String,
    val description: String = "",
    val fileUri: Uri? = null,
    val filePath: String? = null
) {
    companion object {
        // Создание объекта Book из файла
        fun fromFile(file: File): Book {
            val title = file.nameWithoutExtension
            return Book(
                id = title,
                title = title,
                description = "Импортированная книга: $title",
                filePath = file.absolutePath
            )
        }

        // Создание объекта Book из Uri
        fun fromUri(uri: Uri, title: String, filePath: String): Book {
            return Book(
                id = title.replace(" ", "_"),
                title = title,
                description = "Импортированная книга: $title",
                fileUri = uri,
                filePath = filePath
            )
        }
    }
}
