package com.example.ui.chatlist

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.model.MoodType
import com.example.ui.HomiesViewModel
import com.example.ui.NavTab
import com.example.ui.components.HomieAvatar
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleGlow
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
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
fun ChatListScreen(
    viewModel: HomiesViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showNewChatMenu by remember { mutableStateOf(false) }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.lastMessageText.contains(searchQuery, ignoreCase = true)
        }
    }

    val theme = com.example.ui.theme.LocalAppTheme.current
    var showThemeMenu by remember { mutableStateOf(false) }

    val currentMoodType = remember(currentUser?.mood) {
        MoodType.entries.find { it.name == currentUser?.mood } ?: MoodType.HAPPY
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Homies",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("HQ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Theme Switcher Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceCardElevated)
                            .border(1.dp, theme.primaryLight, CircleShape)
                            .clickable { showThemeMenu = true }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(theme.icon, fontSize = 16.sp)
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false },
                        modifier = Modifier.background(theme.surfaceCard)
                    ) {
                        com.example.ui.theme.AppTheme.entries.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(t.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(t.title, color = if (theme == t) t.primaryLight else TextPrimary, fontWeight = if (theme == t) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    showThemeMenu = false
                                    viewModel.setAppTheme(t)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mood Chip (Tap to change mood in Profile)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surfaceCardElevated)
                        .border(1.dp, Color(currentMoodType.colorHex).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.setTab(NavTab.PROFILE) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("mood_status_chip")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentMoodType.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentMoodType.title,
                            color = Color(currentMoodType.colorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // New Chat / Group Button
                Box {
                    IconButton(
                        onClick = { showNewChatMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.primary)
                            .testTag("new_chat_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showNewChatMenu,
                        onDismissRequest = { showNewChatMenu = false },
                        modifier = Modifier.background(theme.surfaceCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Group Chat 👥", color = TextPrimary) },
                            onClick = {
                                showNewChatMenu = false
                                viewModel.showCreateGroupDialog.value = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Find Homie 👋", color = TextPrimary) },
                            onClick = {
                                showNewChatMenu = false
                                viewModel.setTab(NavTab.HOMIES)
                            }
                        )
                    }
                }
            }
        }

        // Stories Bar
        StoriesHorizontalRow(
            stories = stories,
            currentUserName = currentUser?.displayName ?: "You",
            currentUserAvatar = currentUser?.avatarUrl ?: "avatar_1",
            onAddStory = { viewModel.showCreateStoryDialog.value = true },
            onViewStory = { story -> viewModel.viewStory(story) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search homies and group chats...", fontSize = 13.sp, color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = ElectricPurpleLight,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chats List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredChats, key = { it.id }) { chat ->
                ChatItemRow(
                    chat = chat,
                    onClick = { viewModel.openChat(chat.id) },
                    onTogglePin = { viewModel.togglePin(chat.id) },
                    onToggleMute = { viewModel.toggleMute(chat.id) }
                )
            }

            if (filteredChats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💬", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No chats found", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Tap + to start talking to your homies!", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoriesHorizontalRow(
    stories: List<StoryEntity>,
    currentUserName: String,
    currentUserAvatar: String,
    onAddStory: () -> Unit,
    onViewStory: (StoryEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        // "Add Story" circle
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onAddStory() }
            ) {
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HomieAvatar(
                        avatarKey = currentUserAvatar,
                        displayName = currentUserName,
                        size = 50.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(ElectricPurple)
                            .border(2.dp, DeepBlack, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Story", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
        }

        // Friends' Stories
        items(stories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onViewStory(story) }
            ) {
                HomieAvatar(
                    avatarKey = story.userAvatar,
                    displayName = story.userDisplayName,
                    size = 50.dp,
                    hasStoryRing = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.userDisplayName.split(" ").firstOrNull() ?: story.userDisplayName,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: ChatEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(chat.lastMessageTime) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(chat.lastMessageTime))
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("chat_row_${chat.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomieAvatar(
                avatarKey = chat.avatarUrl,
                displayName = chat.title,
                size = 52.dp,
                isOnline = chat.type == "DIRECT"
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isChaosMode) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("😈", fontSize = 11.sp)
                        }
                        if (chat.isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = ElectricPurpleGlow, modifier = Modifier.size(13.dp))
                        }
                    }

                    Text(
                        text = formattedTime,
                        color = if (chat.unreadCount > 0) NeonCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessageText.ifBlank { "Tap to chat" },
                        color = if (chat.unreadCount > 0) TextPrimary else TextSecondary,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (chat.isMuted) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = "Muted", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ElectricPurple)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Long press / dropdown
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(SurfaceCard)
        ) {
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) "Unpin Chat" else "Pin Chat 📌", color = TextPrimary) },
                onClick = {
                    showContextMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isMuted) "Unmute" else "Mute Chat 🔕", color = TextPrimary) },
                onClick = {
                    showContextMenu = false
                    onToggleMute()
                }
            )
        }
    }
}
