package com.taoufikcode.krosschat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.taoufikcode.core.designsystem.theme.KrossChatTheme
import com.taoufikcode.krosschat.navigation.DeepLinkListener
import com.taoufikcode.krosschat.navigation.NavigationRoot
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController)
    KrossChatTheme {
        NavigationRoot(navController)
    }
}