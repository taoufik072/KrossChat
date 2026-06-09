package com.taoufikcode.chat.presentation.mappers

import com.taoufikcode.chat.domain.models.ChatParticipant
import com.taoufikcode.core.designsystem.components.avatar.ChatParticipantUi
import com.taoufikcode.core.domain.auth.User

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )
}
fun User.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = id,
        username = userName,
        initials = userName.take(2).uppercase(),
        imageUrl = profilePictureUrl
    )
}