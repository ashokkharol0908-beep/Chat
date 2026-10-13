package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val password: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val mood: String = "HAPPY",
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val anonMessageUsedToday: Boolean = false
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String, // DIRECT or GROUP
    val title: String,
    val avatarUrl: String,
    val participantUsernames: String, // comma-separated
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isChaosMode: Boolean = false,
    val isVoiceRoomActive: Boolean = false,
    val voiceRoomMembers: String = "" // comma-separated
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderUsername: String,
    val senderDisplayName: String,
    val senderAvatar: String,
    val type: String, // TEXT, VOICE, IMAGE, MEME, VIBE_CHALLENGE, CHAOS_SYSTEM
    val text: String,
    val mediaUri: String? = null,
    val durationSec: Int? = null,
    val waveformData: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: String = "", // e.g., "❤️:user1;🔥:user2"
    val status: String = "READ",
    val timestamp: Long = System.currentTimeMillis(),
    val isAnonymous: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val username: String,
    val userDisplayName: String,
    val userAvatar: String,
    val type: String, // TEXT, IMAGE, VOICE
    val content: String,
    val mediaUri: String? = null,
    val voiceDurationSec: Int? = null,
    val backgroundColorHex: Long = 0xFF7E22CE,
    val createdAt: Long = System.currentTimeMillis(),
    val viewerCount: Int = 1
)
