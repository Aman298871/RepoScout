package com.example.reposcout.util

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null, val isRateLimited: Boolean = false) : AppResult<Nothing>()
}
