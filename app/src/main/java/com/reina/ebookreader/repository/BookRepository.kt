// File: BookRepository.kt
package com.reina.ebookreader.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.reina.ebookreader.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset
import kotlin.text.Charsets

class BookRepository(private val context: Context) {

    private val TAG = "BookRepository"

    // SharedPreferences для хранения офсетов прокрутки по bookId
    private val prefs = context.getSharedPreferences("scroll_prefs", Context.MODE_PRIVATE)

    // Список книг для UI
    private val _books = mutableStateListOf<Book>()
    val books: List<Book> = _books

    init {
        loadSavedBooks()
    }

    private fun loadSavedBooks() {
        val dir = File(context.filesDir, "books")
        if (!dir.exists()) dir.mkdirs()
        dir.listFiles { f -> f.isFile && f.extension == "txt" }
            ?.forEach { file ->
                _books.add(Book.fromFile(file))
                Log.d(TAG, "Loaded book: ${file.name}")
            }
    }

    /**
     * Импортирует книгу из URI, копируя «сырые» байты,
     * чтобы сохранить оригинальную кодировку файла.
     */
    suspend fun importBookFromUri(uri: Uri, title: String): Book? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Import start: $title, $uri")
            val dir = File(context.filesDir, "books")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "${title.replace(' ', '_')}.txt"
            val outFile = File(dir, fileName)
            if (outFile.exists()) outFile.delete()

            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Log.e(TAG, "Cannot open URI stream: $uri")
                return@withContext null
            }

            val book = Book.fromFile(outFile)
            _books.add(book)
            Log.d(TAG, "Import success: ${book.title}, id=${book.id}")
            return@withContext book

        } catch (e: Exception) {
            Log.e(TAG, "Import error", e)
            return@withContext null
        }
    }

    /**
     * Читает локальный .txt-файл, детектируя кодировку:
     * перебирает CP1251, KOI8-R, UTF-8 и ISO-8859-1,
     * выбирая вариант с максимальной долей кириллицы.
     */
    suspend fun readBookContent(book: Book): String = withContext(Dispatchers.IO) {
        try {
            val path = book.filePath
                ?: throw IllegalArgumentException("У книги нет пути к файлу")
            val file = File(path)
            if (!file.exists()) throw IllegalStateException("Файл не найден: $path")

            // Считаем все байты
            val bytes = file.readBytes()

            // Список кодировок для попытки
            val charsetsToTry = listOf(
                Charset.forName("windows-1251"),
                Charset.forName("KOI8-R"),
                Charsets.UTF_8,
                Charsets.ISO_8859_1
            )

            // Оцениваем каждый вариант
            data class Candidate(val text: String, val score: Double)
            val best = charsetsToTry
                .map { cs ->
                    val txt = try { String(bytes, cs) } catch (_: Exception) { "" }
                    val cyr = txt.count { it in '\u0400'..'\u04FF' }
                    val score = if (txt.isEmpty()) 0.0 else cyr.toDouble() / txt.length
                    Candidate(txt, score)
                }
                .maxByOrNull { it.score }
                ?: Candidate("Не удалось прочитать текст", 0.0)

            Log.d(TAG, "readBookContent: выбран score=${best.score}")
            return@withContext best.text

        } catch (e: Exception) {
            Log.e(TAG, "Read error", e)
            return@withContext "Ошибка чтения: ${e.localizedMessage}"
        }
    }

    /**
     * Удаляет книгу из списка и файл из storage.
     */
    fun removeBook(book: Book) {
        try {
            _books.remove(book)
            book.filePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete().also { Log.d(TAG, "Deleted file: $path, result=$it") }
                }
                // При удалении книги также очистим сохранённый офсет
                prefs.edit().remove(book.id).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remove error", e)
        }
    }

    /** Сохраняет для конкретной книги позицию прокрутки (в пикселях). */
    fun saveScrollOffset(bookId: String, offset: Int) {
        prefs.edit()
            .putInt(bookId, offset)
            .apply()
        Log.d(TAG, "Repository: saved scrollOffset=$offset for bookId=$bookId")
    }

    /** Загружает сохранённую позицию прокрутки (0, если нет). */
    fun loadScrollOffset(bookId: String): Int {
        val offset = prefs.getInt(bookId, 0)
        Log.d(TAG, "Repository: loaded scrollOffset=$offset for bookId=$bookId")
        return offset
    }
}
