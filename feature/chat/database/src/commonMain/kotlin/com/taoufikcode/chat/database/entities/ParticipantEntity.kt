package com.taoufikcode.chat.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParticipantEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val profilePictureUrl: String?
)
