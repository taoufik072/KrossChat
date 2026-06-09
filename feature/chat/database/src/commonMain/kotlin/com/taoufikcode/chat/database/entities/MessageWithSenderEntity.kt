package com.taoufikcode.chat.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithSenderEntity(
    @Embedded
    val message: MessageEntity,
    @Relation(
        parentColumn = "senderId",
        entityColumn = "userId"
    )
    val sender: ParticipantEntity
)
