
package com.taoufikcode.core.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner

@Composable
fun DialogSheetScopedViewModel(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    if (!visible) return

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides rememberViewModelStoreOwner()
    ) {
        content()
    }
}
