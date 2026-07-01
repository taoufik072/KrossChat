package com.taoufikcode.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String
)
