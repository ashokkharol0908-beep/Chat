package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HomiesDatabase
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.VibeType
import com.example.data.repository.HomiesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NavTab {
    CHATS,
    STORIES,
    HOMIES,
    PROFILE
}

class HomiesViewModel(application: Application) : AndroidViewModel(application) {
    val database = HomiesDatabase.getDatabase(application)
    val repository = HomiesRepository(database.homiesDao())

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
    val typingStatus: StateFlow<Map<String, String>> = repository.typingStatus

    val chats: StateFlow<List<ChatEntity>> = repository.getChatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.getStoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Screen & Chat
    private val _currentTab = MutableStateFlow(NavTab.CHATS)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private var messagesJob: Job? = null

    // Dynamic Theme
    val currentAppTheme = MutableStateFlow(com.example.ui.theme.AppTheme.ELECTRIC_PURPLE)

    fun setAppTheme(theme: com.example.ui.theme.AppTheme) {
        currentAppTheme.value = theme
    }

    // Dialogs & Modals
    val showVibeDialog = MutableStateFlow(false)
    val showSpinBottleDialog = MutableStateFlow(false)
    val showMemeMakerDialog = MutableStateFlow(false)
    val showCreateGroupDialog = MutableStateFlow(false)
    val showCreateStoryDialog = MutableStateFlow(false)
    val showGroupSettingsDialog = MutableStateFlow(false)
    val selectedStory = MutableStateFlow<StoryEntity?>(null)

    // Message Input State
    val replyToMessage = MutableStateFlow<MessageEntity?>(null)
    val isAnonymousActive = MutableStateFlow(false)
    val isRecording = MutableStateFlow(false)
    val recordingDuration = MutableStateFlow(0)
    val liveRecordingWaveform = MutableStateFlow<List<Float>>(emptyList())

    // Auth Form State
    val isAuthLogin = MutableStateFlow(true)
    val authUsername = MutableStateFlow("")
    val authPassword = MutableStateFlow("")
    val authDisplayName = MutableStateFlow("")
    val authError = MutableStateFlow<String?>(null)

    private var recordingJob: Job? = null

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForChatFlow(chatId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun closeChat() {
        _activeChatId.value = null
        messagesJob?.cancel()
        replyToMessage.value = null
        isAnonymousActive.value = false
    }

    // --- Auth Actions ---
    fun submitAuth() {
        authError.value = null
        val username = authUsername.value.trim()
        val password = authPassword.value.trim()
        if (username.isBlank() || password.isBlank()) {
            authError.value = "Username and password cannot be blank"
            return
        }

        viewModelScope.launch {
            if (isAuthLogin.value) {
                val res = repository.login(username, password)
                if (res.isFailure) {
                    authError.value = res.exceptionOrNull()?.message ?: "Login failed"
                } else {
                    authUsername.value = ""
                    authPassword.value = ""
                }
            } else {
                val res = repository.signup(
                    username = username,
                    pass = password,
                    displayName = authDisplayName.value.ifBlank { username }
                )
                if (res.isFailure) {
                    authError.value = res.exceptionOrNull()?.message ?: "Signup failed"
                } else {
                    authUsername.value = ""
                    authPassword.value = ""
                    authDisplayName.value = ""
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _activeChatId.value = null
    }

    fun switchAccount(username: String) {
        viewModelScope.launch {
            repository.switchUser(username)
        }
    }

    // --- Chat Actions ---
    fun sendText(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendTextMessage(
                chatId = chatId,
                text = text,
                replyTo = replyToMessage.value,
                isAnonymous = isAnonymousActive.value
            )
            replyToMessage.value = null
            if (isAnonymousActive.value) {
                isAnonymousActive.value = false // reset one-time daily anon toggle
            }
        }
    }

    fun startVoiceRecording() {
        isRecording.value = true
        recordingDuration.value = 0
        liveRecordingWaveform.value = listOf(0.3f, 0.5f, 0.4f)

        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (isRecording.value) {
                delay(1000)
                recordingDuration.value += 1
                val newWave = liveRecordingWaveform.value.toMutableList()
                newWave.add(Random.nextFloat() * 0.8f + 0.2f)
                if (newWave.size > 20) newWave.removeAt(0)
                liveRecordingWaveform.value = newWave
            }
        }
    }

    fun finishVoiceRecording(cancel: Boolean = false) {
        val chatId = _activeChatId.value
        val duration = recordingDuration.value
        val wave = liveRecordingWaveform.value
        isRecording.value = false
        recordingJob?.cancel()

        if (!cancel && chatId != null && duration >= 1) {
            viewModelScope.launch {
                val finalWave = if (wave.size < 8) {
                    List(10) { Random.nextFloat() * 0.7f + 0.3f }
                } else wave
                repository.sendVoiceMessage(chatId, duration, finalWave)
            }
        }
        recordingDuration.value = 0
        liveRecordingWaveform.value = emptyList()
    }

    fun sendImage(uri: String, caption: String = "") {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendImageMessage(chatId, uri, caption)
        }
    }

    fun sendMeme(content: String, isMeme: Boolean = true) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendMemeOrSticker(chatId, content, isMeme)
        }
    }

    fun sendVibe(vibeType: VibeType, customText: String? = null) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendVibeAction(chatId, vibeType, customText)
        }
    }

    fun triggerSpinBottle() {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.spinBottle(chatId)
        }
    }

    fun triggerChaosNow() {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.triggerChaosEvent(chatId)
        }
    }

    fun toggleVoiceRoom() {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.toggleVoiceRoom(chatId)
        }
    }

    fun toggleChaosMode(enabled: Boolean) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.toggleChaosMode(chatId, enabled)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessageForEveryone(messageId)
        }
    }

    fun addReaction(message: MessageEntity, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(message, emoji)
        }
    }

    fun togglePin(chatId: String) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun toggleMute(chatId: String) {
        viewModelScope.launch {
            repository.toggleMuteChat(chatId)
        }
    }

    fun createGroup(title: String, participants: List<String>) {
        viewModelScope.launch {
            val newId = repository.createGroup(title, participants)
            if (newId.isNotBlank()) {
                openChat(newId)
            }
        }
    }

    fun startDirectChat(otherUsername: String) {
        viewModelScope.launch {
            val chatId = repository.createDirectChatWith(otherUsername)
            if (chatId.isNotBlank()) {
                openChat(chatId)
            }
        }
    }

    // --- Profile & Stories ---
    fun updateMood(mood: String) {
        viewModelScope.launch {
            repository.updateMood(mood)
        }
    }

    fun updateBio(bio: String) {
        viewModelScope.launch {
            repository.updateBio(bio)
        }
    }

    fun updateProfile(displayName: String, avatarUrl: String, bio: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, avatarUrl, bio)
        }
    }

    fun createStory(type: String, content: String, mediaUri: String? = null, voiceSec: Int? = null, bgColor: Long = 0xFF7E22CE) {
        viewModelScope.launch {
            repository.createStory(type, content, mediaUri, voiceSec, bgColor)
        }
    }

    fun viewStory(story: StoryEntity) {
        selectedStory.value = story
        viewModelScope.launch {
            repository.incrementStoryViews(story.id)
        }
    }
}
