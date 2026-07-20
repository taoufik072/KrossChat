package com.taoufikcode.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taoufikcode.chat.presentation.model.ChatUi
import com.taoufikcode.core.designsystem.components.avatar.KrossStackedAvatars
import com.taoufikcode.core.designsystem.theme.extended
import com.taoufikcode.core.designsystem.theme.titleXSmall
import krosschat.feature.chat.presentation.generated.resources.Res
import krosschat.feature.chat.presentation.generated.resources.group_chat
import krosschat.feature.chat.presentation.generated.resources.you
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatItemHeaderRow(
    chat: ChatUi,
    isGroupChat: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChatAvatarCluster(chat = chat)
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChatTitleText(
                chat = chat,
                isGroupChat = isGroupChat,
                modifier = Modifier.fillMaxWidth()
            )
            if (isGroupChat) {
                ChatGroupMembersText(
                    chat = chat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ChatAvatarCluster(
    chat: ChatUi,
    modifier: Modifier = Modifier
) {
    KrossStackedAvatars(
        avatars = chat.otherParticipants.ifEmpty { listOf(chat.currentUser) },
        modifier = modifier
    )
}

@Composable
fun ChatTitleText(
    chat: ChatUi,
    isGroupChat: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (!isGroupChat) {
            chat.otherParticipants.firstOrNull()?.username ?: chat.currentUser.username
        } else {
            stringResource(Res.string.group_chat)
        },
        style = MaterialTheme.typography.titleXSmall,
        color = MaterialTheme.colorScheme.extended.textPrimary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun ChatGroupMembersText(
    chat: ChatUi,
    modifier: Modifier = Modifier
) {
    val you = stringResource(Res.string.you)
    val formattedUsernames = remember(chat.otherParticipants) {
        "$you, " + chat.otherParticipants.joinToString {
            it.username
        }
    }
    Text(
        text = formattedUsernames,
        color = MaterialTheme.colorScheme.extended.textPlaceholder,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}