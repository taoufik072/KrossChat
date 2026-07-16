package com.taoufikcode.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.taoufikcode.chat.database.entities.ChatEntity
import com.taoufikcode.chat.database.entities.ChatInfoEntity
import com.taoufikcode.chat.database.entities.ChatParticipantJoin
import com.taoufikcode.chat.database.entities.ChatWithParticipantsEntity
import com.taoufikcode.chat.database.entities.MessageEntity
import com.taoufikcode.chat.database.entities.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Query("DELETE FROM chatentity WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("UPDATE chatentity SET lastActivityAt = :lastActivityAt WHERE chatId = :chatId")
    suspend fun updateLastActivityAt(chatId: String, lastActivityAt: Long)

    @Query("SELECT * FROM chatentity ORDER BY lastActivityAt DESC")
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipantsEntity>>

    @Query("SELECT * FROM chatentity WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatWithParticipantsEntity?

    @Query("DELETE FROM chatentity")
    suspend fun deleteAllChats()

    @Query("SELECT chatId FROM chatentity")
    suspend fun getAllChatIds(): List<String>

    @Transaction
    suspend fun deleteChatsByIds(chatIds: List<String>) {
        chatIds.forEach { chatId ->
            deleteChatById(chatId)
        }
    }

    @Query("SELECT COUNT(*) FROM chatentity")
    fun getChatCount(): Flow<Int>

    @Query("""
        SELECT p.*
        FROM participantentity p
        JOIN chatparticipantjoin cpcr ON p.userId = cpcr.userId
        WHERE cpcr.chatId = :chatId AND cpcr.isActive = true
        ORDER BY p.username
    """)
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM chatentity WHERE chatId = :chatId")
    @Transaction
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>

    @Transaction
    suspend fun upsertChatWithParticipantsAndCrossRefs(
        chat: ChatEntity,
        participants: List<ParticipantEntity>,
        participantDao: ParticipantDao,
        crossRefDao: ChatParticipantsJoinDao
    ) {
        upsertChat(chat)
        participantDao.upsertParticipants(participants)

        val crossRefs = participants.map {
            ChatParticipantJoin(
                chatId = chat.chatId,
                userId = it.userId,
                isActive = true
            )
        }
        crossRefDao.upsertCrossRefs(crossRefs)
        crossRefDao.syncChatParticipants(chat.chatId, participants)
    }

    @Transaction
    suspend fun upsertChatsWithParticipantsAndCrossRefs(
        chats: List<ChatWithParticipantsEntity>,
        participantDao: ParticipantDao,
        crossRefDao: ChatParticipantsJoinDao,
        messageDao: MessageDao
    ) {
        upsertChats(chats.map { it.chat })

        val serverChatIds = chats.map { it.chat.chatId }
        val localChatIds = getAllChatIds()
        val staleChatIds = (localChatIds - serverChatIds.toSet()).filter { it != "gemini_chat" }

        chats.forEach { chat ->
            chat.lastMessage?.run {
                messageDao.upsertMessage(
                    MessageEntity(
                        messageId = messageId,
                        chatId = chatId,
                        senderId = senderId,
                        content = content,
                        timestamp = timestamp,
                        deliveryStatus = deliveryStatus
                    )
                )
            }
        }

        val allParticipants = chats.flatMap { it.participants }
        participantDao.upsertParticipants(allParticipants)

        val allCrossRefs = chats.flatMap { chatWithParticipants ->
            chatWithParticipants.participants.map { participant ->
                ChatParticipantJoin(
                    chatId = chatWithParticipants.chat.chatId,
                    userId = participant.userId,
                    isActive = true
                )
            }
        }
        crossRefDao.upsertCrossRefs(allCrossRefs)

        chats.forEach { chat ->
            crossRefDao.syncChatParticipants(
                chatId = chat.chat.chatId,
                participants = chat.participants
            )
        }

        deleteChatsByIds(staleChatIds)
    }
}