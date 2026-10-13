package com.example.data.repository

import android.content.Context
import com.example.data.local.HomiesDao
import com.example.data.local.HomiesDatabase
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.ChatType
import com.example.data.model.MessageType
import com.example.data.model.VibeDataBank
import com.example.data.model.VibeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class HomiesRepository(
    private val dao: HomiesDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val defaultAlex = UserEntity(
        username = "alex_v",
        password = "123",
        displayName = "Alex V",
        avatarUrl = "avatar_1",
        bio = "Always down for late night tacos 🌮✨",
        mood = "LOCKED_IN",
        isOnline = true
    )
    private val _currentUser = MutableStateFlow<UserEntity?>(defaultAlex)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, String>>(emptyMap()) // chatId -> typing user displayName
    val typingStatus: StateFlow<Map<String, String>> = _typingStatus.asStateFlow()

    init {
        scope.launch {
            seedDatabaseIfEmpty()
            val existing = dao.getUserByUsername("alex_v")
            if (existing != null) {
                _currentUser.value = existing
            }
        }
    }

    // --- Authentication ---
    suspend fun login(username: String, pass: String): Result<UserEntity> {
        val user = dao.authenticate(username.trim().lowercase(), pass)
        return if (user != null) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    suspend fun signup(
        username: String,
        pass: String,
        displayName: String,
        avatarUrl: String = "avatar_1",
        bio: String = "Living in the moment ✨"
    ): Result<UserEntity> {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.length < 3) {
            return Result.failure(Exception("Username must be at least 3 characters"))
        }
        if (pass.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters"))
        }
        val existing = dao.getUserByUsername(cleanUsername)
        if (existing != null) {
            return Result.failure(Exception("Username is already taken"))
        }

        val newUser = UserEntity(
            username = cleanUsername,
            password = pass,
            displayName = displayName.ifBlank { cleanUsername },
            avatarUrl = avatarUrl,
            bio = bio,
            mood = "HAPPY",
            isOnline = true
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser

        // Automatically add newUser into the squad chat
        val squadChat = dao.getChatById("chat_squad")
        if (squadChat != null) {
            val updatedParticipants = if (squadChat.participantUsernames.contains(cleanUsername)) {
                squadChat.participantUsernames
            } else {
                "${squadChat.participantUsernames},$cleanUsername"
            }
            dao.updateChat(squadChat.copy(participantUsernames = updatedParticipants))
        }

        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun switchUser(targetUsername: String) {
        val user = dao.getUserByUsername(targetUsername)
        if (user != null) {
            _currentUser.value = user
        }
    }

    // --- Users & Profiles ---
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()

    suspend fun updateMood(mood: String) {
        val user = _currentUser.value ?: return
        dao.updateMood(user.username, mood)
        _currentUser.value = user.copy(mood = mood)
    }

    suspend fun updateBio(bio: String) {
        val user = _currentUser.value ?: return
        dao.updateBio(user.username, bio)
        _currentUser.value = user.copy(bio = bio)
    }

    suspend fun updateProfile(displayName: String, avatarUrl: String, bio: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(displayName = displayName, avatarUrl = avatarUrl, bio = bio)
        dao.updateUser(updated)
        _currentUser.value = updated
    }

    // --- Chats ---
    fun getChatsFlow(): Flow<List<ChatEntity>> = dao.getChatsFlow()

    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?> = dao.getChatByIdFlow(chatId)

    suspend fun togglePinChat(chatId: String) = dao.togglePinChat(chatId)

    suspend fun toggleMuteChat(chatId: String) = dao.toggleMuteChat(chatId)

    suspend fun toggleChaosMode(chatId: String, enabled: Boolean) = dao.setChaosMode(chatId, enabled)

    suspend fun toggleVoiceRoom(chatId: String) {
        val chat = dao.getChatById(chatId) ?: return
        val user = _currentUser.value ?: return
        val isCurrentlyActive = chat.isVoiceRoomActive
        if (isCurrentlyActive) {
            // Check if current user is in members
            val members = chat.voiceRoomMembers.split(",").filter { it.isNotBlank() }.toMutableList()
            if (members.contains(user.displayName)) {
                members.remove(user.displayName)
                if (members.isEmpty()) {
                    dao.updateVoiceRoom(chatId, false, "")
                } else {
                    dao.updateVoiceRoom(chatId, true, members.joinToString(","))
                }
            } else {
                members.add(user.displayName)
                dao.updateVoiceRoom(chatId, true, members.joinToString(","))
            }
        } else {
            dao.updateVoiceRoom(chatId, true, "${user.displayName},Maya K,Leo Dash")
        }
    }

    suspend fun createGroup(title: String, participantUsernames: List<String>): String {
        val user = _currentUser.value ?: return ""
        val allParticipants = (participantUsernames + user.username).distinct()
        val newChatId = "group_${UUID.randomUUID().toString().take(8)}"
        val newChat = ChatEntity(
            id = newChatId,
            type = ChatType.GROUP.name,
            title = title,
            avatarUrl = "avatar_group_${(1..4).random()}",
            participantUsernames = allParticipants.joinToString(","),
            lastMessageText = "Group created 🎉",
            lastMessageTime = System.currentTimeMillis(),
            isChaosMode = false
        )
        dao.insertChat(newChat)
        // Insert initial system message
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = newChatId,
                senderUsername = "system",
                senderDisplayName = "Homies",
                senderAvatar = "avatar_system",
                type = MessageType.CHAOS_SYSTEM.name,
                text = "${user.displayName} created group '$title'",
                timestamp = System.currentTimeMillis()
            )
        )
        return newChatId
    }

    suspend fun createDirectChatWith(otherUsername: String): String {
        val user = _currentUser.value ?: return ""
        val other = dao.getUserByUsername(otherUsername) ?: return ""
        val existingChats = dao.getAllUsers() // or query
        val chatId = "direct_${listOf(user.username, other.username).sorted().joinToString("_")}"
        val existing = dao.getChatById(chatId)
        if (existing != null) return chatId

        val newChat = ChatEntity(
            id = chatId,
            type = ChatType.DIRECT.name,
            title = other.displayName,
            avatarUrl = other.avatarUrl,
            participantUsernames = "${user.username},${other.username}",
            lastMessageText = "Tap to start chatting 👋",
            lastMessageTime = System.currentTimeMillis()
        )
        dao.insertChat(newChat)
        return chatId
    }

    // --- Messages ---
    fun getMessagesForChatFlow(chatId: String): Flow<List<MessageEntity>> = dao.getMessagesForChatFlow(chatId)

    suspend fun sendTextMessage(
        chatId: String,
        text: String,
        replyTo: MessageEntity? = null,
        isAnonymous: Boolean = false
    ) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = if (isAnonymous) "anon" else user.username,
            senderDisplayName = if (isAnonymous) "Anonymous Homie 🎭" else user.displayName,
            senderAvatar = if (isAnonymous) "avatar_anon" else user.avatarUrl,
            type = MessageType.TEXT.name,
            text = text.trim(),
            replyToMessageId = replyTo?.id,
            replyToText = replyTo?.text,
            replyToSender = replyTo?.senderDisplayName,
            status = "READ",
            timestamp = System.currentTimeMillis(),
            isAnonymous = isAnonymous
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, if (isAnonymous) "🎭: ${msg.text}" else msg.text, msg.timestamp)

        simulateHomieReply(chatId, text, isGroup = chatId.startsWith("chat_squad") || chatId.startsWith("group_"))
    }

    suspend fun sendVoiceMessage(
        chatId: String,
        durationSec: Int,
        waveform: List<Float>
    ) {
        val user = _currentUser.value ?: return
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = user.username,
            senderDisplayName = user.displayName,
            senderAvatar = user.avatarUrl,
            type = MessageType.VOICE.name,
            text = "Voice message (${durationSec}s)",
            durationSec = durationSec,
            waveformData = waveform.joinToString(",") { String.format("%.2f", it) },
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, "🎤 Voice note (${durationSec}s)", msg.timestamp)

        simulateHomieReply(chatId, "voice_note", isGroup = chatId.contains("squad") || chatId.startsWith("group_"))
    }

    suspend fun sendImageMessage(
        chatId: String,
        imageUri: String,
        caption: String = ""
    ) {
        val user = _currentUser.value ?: return
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = user.username,
            senderDisplayName = user.displayName,
            senderAvatar = user.avatarUrl,
            type = MessageType.IMAGE.name,
            text = caption.ifBlank { "Sent a photo 📷" },
            mediaUri = imageUri,
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, "📷 Photo ${caption.take(15)}", msg.timestamp)

        simulateHomieReply(chatId, "photo", isGroup = chatId.contains("squad") || chatId.startsWith("group_"))
    }

    suspend fun sendMemeOrSticker(
        chatId: String,
        content: String,
        isMeme: Boolean = false
    ) {
        val user = _currentUser.value ?: return
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = user.username,
            senderDisplayName = user.displayName,
            senderAvatar = user.avatarUrl,
            type = if (isMeme) MessageType.MEME.name else MessageType.TEXT.name,
            text = content,
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, if (isMeme) "🖼️ Meme" else content, msg.timestamp)

        simulateHomieReply(chatId, "meme", isGroup = chatId.contains("squad") || chatId.startsWith("group_"))
    }

    suspend fun sendVibeAction(
        chatId: String,
        vibeType: VibeType,
        customPrompt: String? = null
    ) {
        val user = _currentUser.value ?: return
        val promptText = customPrompt ?: when (vibeType) {
            VibeType.DARE -> VibeDataBank.DARES.random()
            VibeType.TRUTH -> VibeDataBank.TRUTHS.random()
            VibeType.ROAST -> VibeDataBank.ROASTS.random()
            VibeType.COMPLIMENT -> VibeDataBank.COMPLIMENTS.random()
        }

        val formattedText = "${vibeType.icon} [${vibeType.label.uppercase()}]\n$promptText"
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = user.username,
            senderDisplayName = user.displayName,
            senderAvatar = user.avatarUrl,
            type = MessageType.VIBE_CHALLENGE.name,
            text = formattedText,
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, "${vibeType.icon} ${vibeType.label}: $promptText", msg.timestamp)

        // Homies always react quickly to Vibe prompts!
        simulateVibeResponse(chatId, vibeType)
    }

    suspend fun spinBottle(chatId: String): String {
        val chat = dao.getChatById(chatId) ?: return "Nobody"
        val participants = chat.participantUsernames.split(",").filter { it.isNotBlank() }
        val users = dao.getAllUsers().filter { participants.contains(it.username) }
        val pickedUser = users.randomOrNull() ?: _currentUser.value ?: return "Someone"

        val dareOrTruth = if (Random.nextBoolean()) {
            "🔥 DARE: " + VibeDataBank.DARES.random()
        } else {
            "🔮 TRUTH: " + VibeDataBank.TRUTHS.random()
        }

        val text = "🍾 BOTTLE SPUN! The bottle pointed directly at ${pickedUser.displayName}! 🎯\n\n$dareOrTruth"
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = "system",
            senderDisplayName = "Spin The Bottle 🍾",
            senderAvatar = "avatar_bottle",
            type = MessageType.VIBE_CHALLENGE.name,
            text = text,
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, "🍾 Bottle landed on ${pickedUser.displayName}!", msg.timestamp)

        // Trigger chosen user response after 2 seconds
        scope.launch {
            delay(2000)
            _typingStatus.value = _typingStatus.value + (chatId to pickedUser.displayName)
            delay(1500)
            _typingStatus.value = _typingStatus.value - chatId
            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderUsername = pickedUser.username,
                senderDisplayName = pickedUser.displayName,
                senderAvatar = pickedUser.avatarUrl,
                type = MessageType.TEXT.name,
                text = listOf(
                    "Bro why is it ALWAYS ME 😭💀 Give me a minute!",
                    "Bet, challenge accepted 😎",
                    "I plead the fifth!! 🤐",
                    "No wayyy lmao okay hold up..."
                ).random(),
                status = "READ",
                timestamp = System.currentTimeMillis()
            )
            dao.insertMessage(replyMsg)
            dao.updateLastMessage(chatId, replyMsg.text, replyMsg.timestamp)
        }

        return pickedUser.displayName
    }

    suspend fun triggerChaosEvent(chatId: String) {
        val challenge = VibeDataBank.CHAOS_CHALLENGES.random()
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderUsername = "chaos_bot",
            senderDisplayName = "CHAOS BOT 😈",
            senderAvatar = "avatar_chaos",
            type = MessageType.CHAOS_SYSTEM.name,
            text = challenge,
            status = "READ",
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(chatId, "😈 $challenge", msg.timestamp)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        dao.softDeleteMessage(messageId)
    }

    suspend fun toggleReaction(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        // Parse existing reactions
        val chatMessages = dao.getMessagesForChatFlow("").toString() // or just single fetch
        // For efficiency in DAO, we can update directly
        // We'll update the reactions string
        // Let's implement reaction toggle
    }

    suspend fun addReaction(message: MessageEntity, emoji: String) {
        val user = _currentUser.value ?: return
        val currentReactions = message.reactions
        // Format: "❤️:user1,user2;🔥:user3"
        val reactionMap = currentReactions.split(";")
            .filter { it.contains(":") }
            .associate {
                val parts = it.split(":")
                val e = parts[0]
                val users = parts[1].split(",").filter { u -> u.isNotBlank() }.toMutableList()
                e to users
            }.toMutableMap()

        val list = reactionMap.getOrPut(emoji) { mutableListOf() }
        if (list.contains(user.username)) {
            list.remove(user.username)
            if (list.isEmpty()) {
                reactionMap.remove(emoji)
            }
        } else {
            list.add(user.username)
        }

        val newReactionString = reactionMap.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}" }
        dao.updateReactions(message.id, newReactionString)
    }

    // --- Stories ---
    fun getStoriesFlow(): Flow<List<StoryEntity>> {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours
        return dao.getRecentStoriesFlow(cutoff)
    }

    suspend fun createStory(
        type: String,
        content: String,
        mediaUri: String? = null,
        voiceDuration: Int? = null,
        bgColor: Long = 0xFF7E22CE
    ) {
        val user = _currentUser.value ?: return
        val story = StoryEntity(
            id = UUID.randomUUID().toString(),
            username = user.username,
            userDisplayName = user.displayName,
            userAvatar = user.avatarUrl,
            type = type,
            content = content,
            mediaUri = mediaUri,
            voiceDurationSec = voiceDuration,
            backgroundColorHex = bgColor,
            createdAt = System.currentTimeMillis()
        )
        dao.insertStory(story)
    }

    suspend fun incrementStoryViews(storyId: String) {
        dao.incrementStoryViews(storyId)
    }

    // --- Simulated Homie Interaction ---
    private fun simulateHomieReply(chatId: String, triggerContext: String, isGroup: Boolean) {
        scope.launch {
            delay(1000)
            val responder = if (isGroup) {
                listOf(
                    Pair("Leo Dash", "leo_dash"),
                    Pair("Maya K", "maya_k"),
                    Pair("Chloe B", "chloe_b")
                ).random()
            } else {
                if (chatId.contains("maya")) Pair("Maya K", "maya_k")
                else if (chatId.contains("leo")) Pair("Leo Dash", "leo_dash")
                else Pair("Chloe B", "chloe_b")
            }

            _typingStatus.value = _typingStatus.value + (chatId to responder.first)
            delay(1600 + Random.nextLong(1000))
            _typingStatus.value = _typingStatus.value - chatId

            val replyText = when {
                triggerContext.contains("voice") -> listOf(
                    "LMAOO your voice sounds completely different today 🎙️😂",
                    "Wait say that again I had my volume on 0% 💀",
                    "Certified podcast host energy right there 🔥",
                    "I felt that in my soul fr 🤝"
                ).random()
                triggerContext.contains("photo") -> listOf(
                    "NO WAY you actually posted that 😭💀💀",
                    "Instant wallpaper material tbh ✨",
                    "Where was this taken?? Drop the location right now 📍",
                    "Iconic homie moment captured in 4K 📸"
                ).random()
                triggerContext.contains("meme") -> listOf(
                    "STOOOP I'm crying at this meme 😭😭😭",
                    "Stealing this for my story immediately 🫡",
                    "Accurate depiction of our squad 💀💯",
                    "Bro found the most cursed template ever 😂"
                ).random()
                else -> listOf(
                    "Facts honestly! 😂💯",
                    "Wait you're so real for saying that 💀",
                    "I was literally thinking the exact same thing 5 mins ago!",
                    "Hol' up let him cook 👨‍🍳🔥",
                    "Who gave you permission to be this funny today? 😭",
                    "Bro what are you even talking about lmao 💀",
                    "Squad meeting at 8 PM, nobody be late 🍕",
                    "Classic homie behavior right there ✨"
                ).random()
            }

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderUsername = responder.second,
                senderDisplayName = responder.first,
                senderAvatar = "avatar_${responder.second}",
                type = MessageType.TEXT.name,
                text = replyText,
                status = "READ",
                timestamp = System.currentTimeMillis()
            )
            dao.insertMessage(replyMsg)
            dao.updateLastMessage(chatId, replyMsg.text, replyMsg.timestamp)
        }
    }

    private fun simulateVibeResponse(chatId: String, vibeType: VibeType) {
        scope.launch {
            delay(1200)
            val responderName = listOf("Leo Dash", "Maya K", "Chloe B", "Sammy").random()
            _typingStatus.value = _typingStatus.value + (chatId to responderName)
            delay(1800)
            _typingStatus.value = _typingStatus.value - chatId

            val reply = when (vibeType) {
                VibeType.DARE -> listOf(
                    "Oh you're wild for choosing this dare 💀 Doing it right now!",
                    "BET. Give me 30 seconds to set this up 🎬",
                    "I refuse without my lawyer present 😭🤣",
                    "Say less, watch this 🔥"
                ).random()
                VibeType.TRUTH -> listOf(
                    "Don't expose me like this in front of everyone 🫣",
                    "Alright fine... the truth is I was actually sleeping the whole time 😴",
                    "I cannot confirm nor deny these allegations 🤐",
                    "100% yes, and I have zero regrets 💀"
                ).random()
                VibeType.ROAST -> listOf(
                    "EMOTIONAL DAMAGE 💀 I will never recover from this",
                    "I took that personally!! Check your mailbox tomorrow 😭",
                    "Okay that one actually hurt a little bit lmao 💀🪦",
                    "Bro woke up and chose pure violence today 🩸"
                ).random()
                VibeType.COMPLIMENT -> listOf(
                    "Awwww homies for life! 🥹❤️❤️",
                    "Stop it you're making me blush in the chat ✨",
                    "Reciprocated 100%! Squad wouldn't be the same without you 👑",
                    "Common W homie affirmation 💫"
                ).random()
            }

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderUsername = responderName.lowercase().replace(" ", "_"),
                senderDisplayName = responderName,
                senderAvatar = "avatar_${responderName.take(3).lowercase()}",
                type = MessageType.TEXT.name,
                text = reply,
                status = "READ",
                timestamp = System.currentTimeMillis()
            )
            dao.insertMessage(replyMsg)
            dao.updateLastMessage(chatId, replyMsg.text, replyMsg.timestamp)
        }
    }

    // --- Initial Database Seeding ---
    private suspend fun seedDatabaseIfEmpty() {
        val existingUsers = dao.getAllUsers()
        if (existingUsers.isNotEmpty()) return

        val users = listOf(
            UserEntity(
                username = "alex_v",
                password = "123",
                displayName = "Alex V",
                avatarUrl = "avatar_1",
                bio = "Always down for late night tacos 🌮✨",
                mood = "LOCKED_IN",
                isOnline = true
            ),
            UserEntity(
                username = "maya_k",
                password = "123",
                displayName = "Maya K",
                avatarUrl = "avatar_2",
                bio = "Probably at a concert or caffeinated ☕",
                mood = "CHILL",
                isOnline = true
            ),
            UserEntity(
                username = "leo_dash",
                password = "123",
                displayName = "Leo Dash",
                avatarUrl = "avatar_3",
                bio = "Chief Meme Officer & late texter 💀",
                mood = "CHAOS",
                isOnline = true
            ),
            UserEntity(
                username = "chloe_b",
                password = "123",
                displayName = "Chloe B",
                avatarUrl = "avatar_4",
                bio = "Living life in 120fps ✨📸",
                mood = "PARTY",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 1000 * 60 * 14
            ),
            UserEntity(
                username = "sam_sam",
                password = "123",
                displayName = "Sammy",
                avatarUrl = "avatar_5",
                bio = "Sleeping or building robots 🤖💤",
                mood = "SLEEPY",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 1000 * 60 * 45
            )
        )
        dao.insertUsers(users)

        val chats = listOf(
            ChatEntity(
                id = "chat_squad",
                type = ChatType.GROUP.name,
                title = "The Inner Circle ⚡",
                avatarUrl = "avatar_group_1",
                participantUsernames = "alex_v,maya_k,leo_dash,chloe_b,sam_sam",
                lastMessageText = "Leo: Bro who is ordering food tonight? 🍕",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 2,
                unreadCount = 2,
                isPinned = true,
                isChaosMode = true,
                isVoiceRoomActive = true,
                voiceRoomMembers = "Maya K,Leo Dash"
            ),
            ChatEntity(
                id = "chat_maya",
                type = ChatType.DIRECT.name,
                title = "Maya K",
                avatarUrl = "avatar_2",
                participantUsernames = "alex_v,maya_k",
                lastMessageText = "Listen to this audio snippet when you get a chance! 🎧",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 18,
                unreadCount = 1,
                isPinned = true
            ),
            ChatEntity(
                id = "chat_leo",
                type = ChatType.DIRECT.name,
                title = "Leo Dash",
                avatarUrl = "avatar_3",
                participantUsernames = "alex_v,leo_dash",
                lastMessageText = "💀 I just saw the wildest meme",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 95,
                unreadCount = 0
            ),
            ChatEntity(
                id = "chat_chloe",
                type = ChatType.DIRECT.name,
                title = "Chloe B",
                avatarUrl = "avatar_4",
                participantUsernames = "alex_v,chloe_b",
                lastMessageText = "Did you get the invite for Saturday? 🎉",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 240,
                unreadCount = 0
            ),
            ChatEntity(
                id = "chat_arcade",
                type = ChatType.GROUP.name,
                title = "Midnight Arcade 🎮",
                avatarUrl = "avatar_group_2",
                participantUsernames = "alex_v,leo_dash,sam_sam",
                lastMessageText = "Sammy: Ranked lobby in 10 mins hop on",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 360,
                unreadCount = 0
            )
        )
        dao.insertChats(chats)

        // Seed Messages for The Inner Circle
        val baseTime = System.currentTimeMillis() - 1000 * 60 * 60
        val squadMessages = listOf(
            MessageEntity(
                id = "msg_1",
                chatId = "chat_squad",
                senderUsername = "maya_k",
                senderDisplayName = "Maya K",
                senderAvatar = "avatar_2",
                type = MessageType.TEXT.name,
                text = "Yo squad! Are we still hitting the rooftop spot this weekend?",
                status = "READ",
                timestamp = baseTime + 1000 * 60 * 5
            ),
            MessageEntity(
                id = "msg_2",
                chatId = "chat_squad",
                senderUsername = "chloe_b",
                senderDisplayName = "Chloe B",
                senderAvatar = "avatar_4",
                type = MessageType.TEXT.name,
                text = "YES 100%! I already bought concert tickets for later too ✨",
                status = "READ",
                timestamp = baseTime + 1000 * 60 * 10
            ),
            MessageEntity(
                id = "msg_3",
                chatId = "chat_squad",
                senderUsername = "alex_v",
                senderDisplayName = "Alex V",
                senderAvatar = "avatar_1",
                type = MessageType.VOICE.name,
                text = "Voice message (7s)",
                durationSec = 7,
                waveformData = "0.2,0.5,0.8,0.9,0.7,0.4,0.6,0.85,0.75,0.4,0.3,0.6,0.3",
                status = "READ",
                timestamp = baseTime + 1000 * 60 * 20
            ),
            MessageEntity(
                id = "msg_4",
                chatId = "chat_squad",
                senderUsername = "leo_dash",
                senderDisplayName = "Leo Dash",
                senderAvatar = "avatar_3",
                type = MessageType.VIBE_CHALLENGE.name,
                text = "🔥 [DARE]\nSend your most unhinged camera roll photo right now with 0 context 📸",
                status = "READ",
                timestamp = baseTime + 1000 * 60 * 35,
                reactions = "🔥:alex_v,maya_k;💀:chloe_b"
            ),
            MessageEntity(
                id = "msg_5",
                chatId = "chat_squad",
                senderUsername = "chaos_bot",
                senderDisplayName = "CHAOS BOT 😈",
                senderAvatar = "avatar_chaos",
                type = MessageType.CHAOS_SYSTEM.name,
                text = "🚨 CHAOS MODE: The next person to send a message must speak exclusively in rhymes for 10 minutes!",
                status = "READ",
                timestamp = baseTime + 1000 * 60 * 45
            ),
            MessageEntity(
                id = "msg_6",
                chatId = "chat_squad",
                senderUsername = "leo_dash",
                senderDisplayName = "Leo Dash",
                senderAvatar = "avatar_3",
                type = MessageType.TEXT.name,
                text = "Bro who is ordering food tonight? 🍕",
                status = "READ",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 2
            )
        )
        dao.insertMessages(squadMessages)

        // Seed Maya Messages
        val mayaMessages = listOf(
            MessageEntity(
                id = "msg_m1",
                chatId = "chat_maya",
                senderUsername = "alex_v",
                senderDisplayName = "Alex V",
                senderAvatar = "avatar_1",
                type = MessageType.TEXT.name,
                text = "Hey Maya, how's that new mix coming along?",
                status = "READ",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            MessageEntity(
                id = "msg_m2",
                chatId = "chat_maya",
                senderUsername = "maya_k",
                senderDisplayName = "Maya K",
                senderAvatar = "avatar_2",
                type = MessageType.VOICE.name,
                text = "Voice message (12s)",
                durationSec = 12,
                waveformData = "0.3,0.4,0.6,0.9,0.95,0.7,0.5,0.6,0.8,0.7,0.5,0.3,0.6,0.8,0.4,0.2",
                status = "READ",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20
            ),
            MessageEntity(
                id = "msg_m3",
                chatId = "chat_maya",
                senderUsername = "maya_k",
                senderDisplayName = "Maya K",
                senderAvatar = "avatar_2",
                type = MessageType.TEXT.name,
                text = "Listen to this audio snippet when you get a chance! 🎧",
                status = "READ",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 18
            )
        )
        dao.insertMessages(mayaMessages)

        // Seed Stories
        val stories = listOf(
            StoryEntity(
                id = "story_1",
                username = "maya_k",
                userDisplayName = "Maya K",
                userAvatar = "avatar_2",
                type = "TEXT",
                content = "Sunset over the skyline while testing the new synthesizer sounds 🌅🎹✨",
                backgroundColorHex = 0xFF9333EA,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                viewerCount = 14
            ),
            StoryEntity(
                id = "story_2",
                username = "leo_dash",
                userDisplayName = "Leo Dash",
                userAvatar = "avatar_3",
                type = "TEXT",
                content = "2 AM cereal simply hits on a celestial frequency 🥣🌙",
                backgroundColorHex = 0xFF06B6D4,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                viewerCount = 22
            ),
            StoryEntity(
                id = "story_3",
                username = "chloe_b",
                userDisplayName = "Chloe B",
                userAvatar = "avatar_4",
                type = "VOICE",
                content = "Vibe check for tonight's squad dinner! 🎙️🥳",
                voiceDurationSec = 8,
                backgroundColorHex = 0xFFF43F5E,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 9,
                viewerCount = 31
            )
        )
        dao.insertStories(stories)
    }
}
