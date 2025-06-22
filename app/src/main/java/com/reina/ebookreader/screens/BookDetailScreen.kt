package com.reina.ebookreader.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reina.ebookreader.viewmodel.BookViewModel

private const val TAG = "BookDetailScreen"

@Composable
fun BookDetailScreen(
    bookId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookViewModel = viewModel()
) {
    // 1) decode ID
    val decodedBookId = runCatching { Uri.decode(bookId) }.getOrDefault(bookId)

    // 2) find book & content
    val book = viewModel.books.find { it.id == decodedBookId }
    val content by viewModel.currentBookContent.collectAsState()
    var bookNotFound by remember { mutableStateOf(false) }

    // 3) prepare a fresh ScrollState
    val scrollState: ScrollState = rememberScrollState()

    // 4) load book when ID changes
    LaunchedEffect(decodedBookId) {
        if (book != null) {
            viewModel.loadBookContent(book)
        } else {
            bookNotFound = true
        }
    }

    // 5) после того как реально загрузился текст (не "Загрузка..."), делаем scrollTo
    LaunchedEffect(content, viewModel.savedScrollOffset) {
        if (content != "Загрузка..." && !content.startsWith("Ошибка") && viewModel.savedScrollOffset > 0) {
            Log.d(TAG, "Re-scrolling to offset = ${viewModel.savedScrollOffset}")
            scrollState.scrollTo(viewModel.savedScrollOffset)
        }
    }

    // 6) обрабатываем кнопку «Назад»
    BackHandler {
        Log.d(TAG, "Saving offset = ${scrollState.value}")
        viewModel.saveScrollPosition(scrollState.value)
        onBackClick()
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            // Шапка
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                IconButton({
                    Log.d(TAG, "Saving offset = ${scrollState.value}")
                    viewModel.saveScrollPosition(scrollState.value)
                    onBackClick()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    text = book?.title ?: "Неизвестная книга",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Контент
            if (bookNotFound) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text("Книга не найдена", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Невозможно найти книгу с ID $decodedBookId.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button({
                        Log.d(TAG, "Saving offset = ${scrollState.value}")
                        viewModel.saveScrollPosition(scrollState.value)
                        onBackClick()
                    }) {
                        Text("Вернуться к списку книг")
                    }
                }

            } else when {
                content == "Загрузка..." -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Загрузка содержимого...")
                    }
                }

                content.startsWith("Ошибка") -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button({
                            Log.d(TAG, "Saving offset = ${scrollState.value}")
                            viewModel.saveScrollPosition(scrollState.value)
                            onBackClick()
                        }) {
                            Text("Вернуться к списку книг")
                        }
                    }
                }

                else -> {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                    )
                }
            }
        }
    }
}
