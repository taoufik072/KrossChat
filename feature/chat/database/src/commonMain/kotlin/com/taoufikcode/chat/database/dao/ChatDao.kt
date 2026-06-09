package com.taoufikcode.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.taoufikcode.chat.database.entities.ChatEntity
import com.taoufikcode.chat.database.entities.ChatInfoEntity
import com.taoufikcode.chat.database.entities.ChatMessageEntity
import com.taoufikcode.chat.database.entities.ChatParticipantCrossRefEntity
import com.taoufikcode.chat.database.entities.ChatParticipantEntity
import com.taoufikcode.chat.database.entities.ChatWithParticipantsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Query("DELETE FROM chatentity WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("SELECT * FROM chatentity ORDER BY lastActivityAt DESC")
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipantsEntity>>

    @Query("""
        SELECT c.*
        FROM chatentity c
        JOIN chatparticipantcrossrefentity cpcr ON c.chatId = cpcr.chatId
        WHERE c.chatId = :chatId AND cpcr.isActive = true
    """)
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
        SELECT c.*
        FROM chatentity c
        WHERE c.chatId = :chatId
    """)
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantEntity>>

    @Query("SELECT * FROM chatentity WHERE chatId = :chatId")
    @Transaction
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>

    @Transaction
    suspend fun upsertChatWithParticipantsAndCrossRefs(
        chat: ChatEntity,
        participants: List<ChatParticipantEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao
    ) {
        upsertChat(chat)
        participantDao.upsertParticipants(participants)

        val crossRefs = participants.map {
            ChatParticipantCrossRefEntity(
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
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao,
        messageDao: ChatMessageDao
    ) {
        upsertChats(chats.map { it.chat })

        val serverChatIds = chats.map { it.chat.chatId }
        val localChatIds = getAllChatIds()
        val staleChatIds = localChatIds - serverChatIds.toSet()

        chats.forEach { chat ->
            chat.lastMessage?.run {
                messageDao.upsertMessage(
                    ChatMessageEntity(
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
                ChatParticipantCrossRefEntity(
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