package com.taoufikcode.chat.data.mappers

import com.taoufikcode.chat.data.dto.ChatParticipantDto
import com.taoufikcode.chat.database.entities.ChatMessageEntity
import com.taoufikcode.chat.database.entities.ChatParticipantEntity
import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.ChatMessageDeliveryStatus
import com.taoufikcode.chat.domain.models.ChatParticipant
import kotlin.time.Instant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}
fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = chatId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )
}

fun ChatParticipantEntity.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipant.toEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}