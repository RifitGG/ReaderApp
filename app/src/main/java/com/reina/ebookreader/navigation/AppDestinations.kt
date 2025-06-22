package com.reina.ebookreader.navigation

import android.net.Uri

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val BOOK_DETAIL_ROUTE = "bookDetail/{bookId}"
    const val SETTINGS_ROUTE = "settings"
    
    fun bookDetailRoute(bookId: String): String {
        return "bookDetail/${Uri.encode(bookId)}"
    }
} 
