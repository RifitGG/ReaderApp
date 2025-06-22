// File: BookViewModel.kt
package com.reina.ebookreader.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reina.ebookreader.model.Book
import com.reina.ebookreader.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "BookViewModel"
    private val repository = BookRepository(application.applicationContext)

    // Список книг
    val books = repository.books

    // Содержимое выбранной книги
    private val _currentBookContent = MutableStateFlow<String>("Загрузка...")
    val currentBookContent: StateFlow<String> = _currentBookContent.asStateFlow()

    // Текущий bookId и сохранённый офсет
    private var currentBookId: String? = null
    var savedScrollOffset: Int = 0
        private set

    // Состояние импорта
    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus: StateFlow<ImportStatus> = _importStatus.asStateFlow()

    /** Импорт книги */
    fun importBook(uri: Uri, title: String) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading
            Log.d(TAG, "Начало импорта книги: $title, URI: $uri")
            val result = repository.importBookFromUri(uri, title)
            _importStatus.value = if (result != null) {
                Log.d(TAG, "Книга успешно импортирована: ${result.title}, путь: ${result.filePath}")
                ImportStatus.Success(result)
            } else {
                Log.e(TAG, "Ошибка импорта книги: $title")
                ImportStatus.Error("Импорт не удался")
            }
        }
    }

    /** Загрузка содержимого книги и её офсета */
    fun loadBookContent(book: Book) {
        currentBookId = book.id
        // Подгружаем офсет из репозитория
        savedScrollOffset = repository.loadScrollOffset(book.id)
        Log.d(TAG, "ViewModel: loaded scrollOffset=$savedScrollOffset for bookId=${book.id}")

        viewModelScope.launch {
            _currentBookContent.value = "Загрузка..."
            Log.d(TAG, "Начало загрузки содержимого: ${book.title}, путь: ${book.filePath}, URI: ${book.fileUri}")

            val fileExists = book.filePath?.let { path ->
                val file = File(path)
                val exists = file.exists() && file.length() > 0
                Log.d(TAG, "Проверка файла: $path, существует: $exists, размер: ${file.length()} байт")
                exists
            } ?: false

            if (!fileExists && book.fileUri == null) {
                Log.e(TAG, "Файл книги не найден или пуст: ${book.title}")
                _currentBookContent.value = "Невозможно загрузить содержимое: файл не найден или пуст"
                return@launch
            }

            try {
                val content = repository.readBookContent(book)
                if (content.isBlank()) {
                    Log.e(TAG, "Содержимое книги пустое: ${book.title}")
                    _currentBookContent.value = "Содержимое книги пустое, проверьте файл"
                } else {
                    Log.d(TAG, "Содержимое книги загружено: ${book.title}, длина: ${content.length}")
                    _currentBookContent.value = content
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке содержимого: ${e.message}", e)
                _currentBookContent.value = "Ошибка загрузки: ${e.localizedMessage}"
            }
        }
    }

    /** Сохраняет офсет и пишет его в репозиторий */
    fun saveScrollPosition(offset: Int) {
        currentBookId?.let { id ->
            savedScrollOffset = offset
            repository.saveScrollOffset(id, offset)
            Log.d(TAG, "ViewModel: saved scrollOffset=$offset for bookId=$id")
        }
    }

    /** Удаление книги */
    fun removeBook(book: Book) {
        repository.removeBook(book)
    }

    /** Сброс статуса импорта */
    fun resetImportStatus() {
        _importStatus.value = ImportStatus.Idle
    }

    /** Класс состояния импорта */
    sealed class ImportStatus {
        object Idle : ImportStatus()
        object Loading : ImportStatus()
        data class Success(val book: Book) : ImportStatus()
        data class Error(val message: String) : ImportStatus()
    }
}
