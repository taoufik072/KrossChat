package com.taoufikcode.chat.data.network

import com.taoufikcode.chat.domain.models.ConnectionState


expect class ConnectionErrorHandler {
    fun getConnectionStateForError(cause: Throwable): ConnectionState
    fun transformException(exception: Throwable): Throwable
    fun isRetriableError(cause: Throwable): Boolean
}