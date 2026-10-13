package com.example.ui.chat

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.model.MessageType
import com.example.ui.HomiesViewModel
import com.example.ui.components.AudioWaveformPlayer
import com.example.ui.components.GroupSettingsDialog
import com.example.ui.components.HomieAvatar
import com.example.ui.components.MemeMakerDialog
import com.example.ui.components.SpinTheBottleDialog
import com.example.ui.components.VibeDialog
import com.example.ui.components.VoiceRoomBar
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleGlow
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ReceivedBubbleColor
import com.example.ui.theme.SentBubbleColor
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TickBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    chat: ChatEntity,
    viewModel: HomiesViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeChat()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val typingStatusMap by viewModel.typingStatus.collectAsState()
    val replyTo by viewModel.replyToMessage.collectAsState()
    val isAnonActive by viewModel.isAnonymousActive.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()

    val showVibe by viewModel.showVibeDialog.collectAsState()
    val showSpinBottle by viewModel.showSpinBottleDialog.collectAsState()
    val showMemeMaker by viewModel.showMemeMakerDialog.collectAsState()
    val showGroupSettings by viewModel.showGroupSettingsDialog.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isGroup = chat.type == "GROUP"
    val typingPerson = typingStatusMap[chat.id]

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendImage(uri.toString())
        }
    }

    // Scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val theme = com.example.ui.theme.LocalAppTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            ChatTopBar(
                chat = chat,
                onBack = { viewModel.closeChat() },
                onOpenSettings = {
                    if (isGroup) viewModel.showGroupSettingsDialog.value = true
                }
            )

            // Sticky Voice Room Banner (if active)
            VoiceRoomBar(
                isActive = chat.isVoiceRoomActive,
                membersSummary = chat.voiceRoomMembers,
                currentUserInRoom = chat.voiceRoomMembers.contains(currentUser?.displayName ?: ""),
                onToggleJoin = { viewModel.toggleVoiceRoom() }
            )

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages, key = { it.id }) { msg ->
                    MessageBubbleItem(
                        message = msg,
                        isSentByMe = msg.senderUsername == currentUser?.username,
                        isGroup = isGroup,
                        onReply = { viewModel.replyToMessage.value = msg },
                        onDelete = { viewModel.deleteMessage(msg.id) },
                        onReact = { emoji -> viewModel.addReaction(msg, emoji) }
                    )
                }

                // Typing indicator animated bubble
                if (typingPerson != null) {
                    item {
                        TypingIndicatorBubble(name = typingPerson)
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // clearance for floating button & input
            }

            // Reply preview banner
            AnimatedVisibility(
                visible = replyTo != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (replyTo != null) {
                    ReplyPreviewBanner(
                        replyTo = replyTo!!,
                        onCancel = { viewModel.replyToMessage.value = null }
                    )
                }
            }

            // Bottom Message Input Bar
            ChatBottomInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendText(inputText)
                        inputText = ""
                    }
                },
                isRecording = isRecording,
                recordingDuration = recordingDuration,
                onStartRecord = { viewModel.startVoiceRecording() },
                onFinishRecord = { viewModel.finishVoiceRecording(cancel = false) },
                onCancelRecord = { viewModel.finishVoiceRecording(cancel = true) },
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onOpenMeme = { viewModel.showMemeMakerDialog.value = true },
                isGroup = isGroup,
                isAnonActive = isAnonActive,
                onToggleAnon = { viewModel.isAnonymousActive.value = !isAnonActive }
            )
        }

        // Floating VIBE Button
        FloatingVibeButton(
            onClick = { viewModel.showVibeDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 74.dp)
        )

        // Modals
        if (showVibe) {
            VibeDialog(
                onDismiss = { viewModel.showVibeDialog.value = false },
                onSendVibe = { type, prompt -> viewModel.sendVibe(type, prompt) },
                onOpenSpinBottle = { viewModel.showSpinBottleDialog.value = true }
            )
        }

        if (showSpinBottle) {
            SpinTheBottleDialog(
                onDismiss = { viewModel.showSpinBottleDialog.value = false },
                onConfirmSpin = { viewModel.triggerSpinBottle() }
            )
        }

        if (showMemeMaker) {
            MemeMakerDialog(
                onDismiss = { viewModel.showMemeMakerDialog.value = false },
                onSendSticker = { sticker -> viewModel.sendMeme(sticker, isMeme = false) },
                onSendMeme = { memeText -> viewModel.sendMeme(memeText, isMeme = true) }
            )
        }

        if (showGroupSettings) {
            GroupSettingsDialog(
                chat = chat,
                onDismiss = { viewModel.showGroupSettingsDialog.value = false },
                onToggleChaos = { enabled -> viewModel.toggleChaosMode(enabled) },
                onTriggerChaosNow = { viewModel.triggerChaosNow() },
                onToggleVoiceRoom = { viewModel.toggleVoiceRoom() }
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    chat: ChatEntity,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            HomieAvatar(
                avatarKey = chat.avatarUrl,
                displayName = chat.title,
                size = 40.dp,
                isOnline = chat.type == "DIRECT"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenSettings() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isChaosMode) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("😈", fontSize = 12.sp)
                    }
                }
                Text(
                    text = if (chat.type == "GROUP") {
                        "${chat.participantUsernames.split(",").size} Homies • tap for settings"
                    } else {
                        "Active now • End-to-end encrypted"
                    },
                    color = NeonCyan,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            if (chat.type == "GROUP") {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Group Settings",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubbleItem(
    message: MessageEntity,
    isSentByMe: Boolean,
    isGroup: Boolean,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionsBar by remember { mutableStateOf(false) }

    val theme = com.example.ui.theme.LocalAppTheme.current

    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isSentByMe) theme.sentBubble else theme.receivedBubble

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = bubbleAlignment
    ) {
        // Group sender label for incoming messages
        if (!isSentByMe && isGroup && !message.isAnonymous && message.type != MessageType.CHAOS_SYSTEM.name) {
            Text(
                text = message.senderDisplayName,
                color = theme.primaryGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }

        Box {
            // Main Bubble Card
            Box(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isSentByMe) 18.dp else 4.dp,
                            bottomEnd = if (isSentByMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (message.type == MessageType.CHAOS_SYSTEM.name) Color(0xFF330C26)
                        else if (message.type == MessageType.VIBE_CHALLENGE.name) Color(0xFF23143B)
                        else bubbleColor
                    )
                    .border(
                        1.dp,
                        if (message.type == MessageType.CHAOS_SYSTEM.name) NeonPink
                        else if (message.type == MessageType.VIBE_CHALLENGE.name) ElectricPurpleLight
                        else SurfaceBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isSentByMe) 18.dp else 4.dp,
                            bottomEnd = if (isSentByMe) 4.dp else 18.dp
                        )
                    )
                    .clickable { showReactionsBar = !showReactionsBar }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("message_bubble_${message.id}")
            ) {
                Column {
                    // Anonymous Tag
                    if (message.isAnonymous) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🎭 Anonymous Homie", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Reply quote box
                    if (message.replyToText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Homie",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = message.replyToText,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Content according to type
                    when (message.type) {
                        MessageType.VOICE.name -> {
                            AudioWaveformPlayer(
                                durationSec = message.durationSec ?: 5,
                                waveformData = message.waveformData,
                                isSentByUser = isSentByMe
                            )
                        }
                        MessageType.IMAGE.name -> {
                            if (!message.mediaUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.mediaUri,
                                    contentDescription = "Image message",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                if (message.text.isNotBlank() && !message.text.startsWith("Sent a photo")) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(message.text, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                        MessageType.MEME.name -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepBlack)
                                    .border(1.dp, ElectricPurple, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        MessageType.VIBE_CHALLENGE.name -> {
                            Column {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = message.text,
                                color = if (message.isDeleted) TextSecondary else TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Timestamp & Read Receipts
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp
                        )

                        if (isSentByMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            // Double blue ticks
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read receipt",
                                tint = TickBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Context Dropdown Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceCard)
            ) {
                DropdownMenuItem(
                    text = { Text("Reply ↩️", color = TextPrimary) },
                    onClick = {
                        showMenu = false
                        onReply()
                    }
                )
                if (isSentByMe && !message.isDeleted) {
                    DropdownMenuItem(
                        text = { Text("Delete for everyone 🗑️", color = NeonPink) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        // Quick Emoji Reaction Bar (toggled on bubble tap)
        AnimatedVisibility(
            visible = showReactionsBar,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCardElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("❤️", "😂", "💀", "🔥", "😮", "👏").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                onReact(emoji)
                                showReactionsBar = false
                            }
                            .padding(4.dp)
                    )
                }
                IconButton(
                    onClick = {
                        showMenu = true
                        showReactionsBar = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Display Attached Emoji Reactions
        if (message.reactions.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.split(";").filter { it.contains(":") }.forEach { reactionItem ->
                    val parts = reactionItem.split(":")
                    val emoji = parts[0]
                    val count = parts.getOrNull(1)?.split(",")?.filter { it.isNotBlank() }?.size ?: 1
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF261D3D))
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$emoji $count",
                            color = TextPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble(name: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ReceivedBubbleColor)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$name is typing",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "💬", fontSize = 12.sp)
    }
}

@Composable
private fun ReplyPreviewBanner(
    replyTo: MessageEntity,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFF19122C),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Reply, contentDescription = null, tint = ElectricPurpleLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Replying to ${replyTo.senderDisplayName}",
                        color = ElectricPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = replyTo.text,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ChatBottomInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isRecording: Boolean,
    recordingDuration: Int,
    onStartRecord: () -> Unit,
    onFinishRecord: () -> Unit,
    onCancelRecord: () -> Unit,
    onPickPhoto: () -> Unit,
    onOpenMeme: () -> Unit,
    isGroup: Boolean,
    isAnonActive: Boolean,
    onToggleAnon: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Anonymous mode pill (in group chat)
            if (isGroup) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAnonActive) NeonPink else Color(0xFF261D3D))
                            .clickable { onToggleAnon() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isAnonActive) "🎭 Anonymous Active (1/1)" else "🎭 Send Anonymously",
                            color = if (isAnonActive) Color.White else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isRecording) {
                // Voice Recording Live Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2D1438))
                        .border(1.dp, NeonPink, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NeonPink)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording 0:${String.format("%02d", recordingDuration)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(onClick = onCancelRecord) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
                        }
                        IconButton(onClick = onFinishRecord) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Voice", tint = NeonCyan)
                        }
                    }
                }
            } else {
                // Normal Text & Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo button
                    IconButton(onClick = onPickPhoto, modifier = Modifier.size(38.dp)) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Photos",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Meme & Stickers button
                    IconButton(onClick = onOpenMeme, modifier = Modifier.size(38.dp)) {
                        Icon(
                            Icons.Default.Mood,
                            contentDescription = "Memes & Stickers",
                            tint = NeonAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onTextChange,
                        placeholder = { Text("Message homies...", fontSize = 13.sp, color = TextSecondary) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF140F24),
                            unfocusedContainerColor = Color(0xFF140F24),
                            focusedBorderColor = ElectricPurple,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isNotBlank()) {
                        // Send Text button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ElectricPurple)
                                .clickable { onSend() }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        // Mic button to record voice note
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF261D3D))
                                .clickable { onStartRecord() }
                                .testTag("record_voice_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Record Voice",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingVibeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = com.example.ui.theme.LocalAppTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(theme.primary, theme.accent)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("floating_vibe_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(theme.icon, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VIBE",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
