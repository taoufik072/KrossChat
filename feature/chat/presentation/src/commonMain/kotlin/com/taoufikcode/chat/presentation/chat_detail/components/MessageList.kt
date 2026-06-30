package com.taoufikcode.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taoufikcode.chat.presentation.chat_list.components.EmptyListSection
import com.taoufikcode.chat.presentation.model.MessageUi
import com.taoufikcode.core.designsystem.components.buttons.KrossButton
import com.taoufikcode.core.designsystem.components.buttons.KrossButtonStyle
import krosschat.feature.chat.presentation.generated.resources.Res
import krosschat.feature.chat.presentation.generated.resources.no_messages
import krosschat.feature.chat.presentation.generated.resources.no_messages_subtitle
import krosschat.feature.chat.presentation.generated.resources.retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<MessageUi>,
    paginationError: String?,
    isPaginationLoading: Boolean,
    messageWithOpenMenu: MessageUi.CurrentUserMessage?,
    listState: LazyListState,
    onMessageLongClick: (MessageUi.CurrentUserMessage) -> Unit,
    onMessageRetryClick: (MessageUi.CurrentUserMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteMessageClick: (MessageUi.CurrentUserMessage) -> Unit,
    onRetryPaginationClick: () -> Unit,
) {
    if (messages.isEmpty()) {
        Box(
            modifier = modifier
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyListSection(
                title = stringResource(Res.string.no_messages),
                description = stringResource(Res.string.no_messages_subtitle),
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                MessageListItemUi(
                    messageUi = message,
                    messageWithOpenMenu = messageWithOpenMenu,
                    onMessageLongClick = onMessageLongClick,
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = onDeleteMessageClick,
                    onRetryClick = onMessageRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
            when {
                isPaginationLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                paginationError != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            KrossButton(
                                text = stringResource(Res.string.retry),
                                onClick = onRetryPaginationClick,
                                style = KrossButtonStyle.SECONDARY
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = paginationError,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}