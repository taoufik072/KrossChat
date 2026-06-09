package com.taoufikcode.chat.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ChatInfoEntity(
    @Embedded
    val chat: ChatEntity,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "userId",
        associateBy = Junction(ChatParticipantJoin::class)
    )
    val participants: List<ParticipantEntity>,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "chatId",
        entity = MessageEntity::class
    )
    val messagesWithSenders: List<MessageWithSenderEntity>
)