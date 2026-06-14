package com.taoufikcode.chat.domain.error

import com.taoufikcode.core.domain.util.Error

enum class ConnectionError: Error {
    NOT_CONNECTED,
    MESSAGE_SEND_FAILED
}