package com.taoufikcode.krosschat

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}