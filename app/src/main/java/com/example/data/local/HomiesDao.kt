package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomiesDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY isOnline DESC, displayName ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET mood = :mood WHERE username = :username")
    suspend fun updateMood(username: String, mood: String)

    @Query("UPDATE users SET bio = :bio WHERE username = :username")
    suspend fun updateBio(username: String, bio: String)

    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE username = :username")
    suspend fun updateAvatar(username: String, avatarUrl: String)

    // --- Chats ---
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getChatsFlow(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET isPinned = NOT isPinned WHERE id = :chatId")
    suspend fun togglePinChat(chatId: String)

    @Query("UPDATE chats SET isMuted = NOT isMuted WHERE id = :chatId")
    suspend fun toggleMuteChat(chatId: String)

    @Query("UPDATE chats SET isChaosMode = :isChaos WHERE id = :chatId")
    suspend fun setChaosMode(chatId: String, isChaos: Boolean)

    @Query("UPDATE chats SET isVoiceRoomActive = :isActive, voiceRoomMembers = :members WHERE id = :chatId")
    suspend fun updateVoiceRoom(chatId: String, isActive: Boolean, members: String)

    @Query("UPDATE chats SET lastMessageText = :text, lastMessageTime = :time WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, time: Long)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isDeleted = 1, text = '🚫 This message was deleted' WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String)

    @Query("UPDATE messages SET reactions = :reactions WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactions: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    // --- Stories ---
    @Query("SELECT * FROM stories WHERE createdAt >= :cutoffTime ORDER BY createdAt DESC")
    fun getRecentStoriesFlow(cutoffTime: Long): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("UPDATE stories SET viewerCount = viewerCount + 1 WHERE id = :storyId")
    suspend fun incrementStoryViews(storyId: String)

    @Query("DELETE FROM stories WHERE createdAt < :cutoffTime")
    suspend fun purgeExpiredStories(cutoffTime: Long)
}
