package com.taoufikcode.core.designsystem.components.brand


import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.taoufikcode.core.designsystem.theme.KrossChatTheme
import krosschat.core.designsystem.generated.resources.Res
import krosschat.core.designsystem.generated.resources.check_icon
import krosschat.core.designsystem.generated.resources.eye_icon
import krosschat.core.designsystem.generated.resources.log_out_icon
import krosschat.core.designsystem.generated.resources.logo_kross
import krosschat.core.designsystem.generated.resources.users_icon
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun KrossBrandLogo(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = vectorResource(Res.drawable.logo_kross),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(48.dp)
    )
}

@Composable
@Preview
fun KrossBrandLogoPre(

) {
    KrossChatTheme {
        KrossBrandLogo()
    }
}