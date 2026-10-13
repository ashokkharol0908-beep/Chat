package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HomiesViewModel
import com.example.ui.NavTab
import com.example.ui.auth.AuthScreen
import com.example.ui.chat.ChatDetailScreen
import com.example.ui.chatlist.ChatListScreen
import com.example.ui.components.CreateGroupDialog
import com.example.ui.homies.HomiesDirectoryScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.story.CreateStoryDialog
import com.example.ui.story.StoryViewerScreen
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HomiesViewModel = viewModel()
            val currentAppTheme by viewModel.currentAppTheme.collectAsState()
            MyApplicationTheme(appTheme = currentAppTheme) {
                HomiesAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HomiesAppRoot(viewModel: HomiesViewModel) {
    val theme = com.example.ui.theme.LocalAppTheme.current
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val selectedStory by viewModel.selectedStory.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val showCreateStory by viewModel.showCreateStoryDialog.collectAsState()
    val showCreateGroup by viewModel.showCreateGroupDialog.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    // 1. If not logged in -> Auth Screen
    if (currentUser == null) {
        AuthScreen(viewModel = viewModel)
        return
    }

    // 2. If viewing a story -> Fullscreen Story Viewer
    if (selectedStory != null) {
        StoryViewerScreen(
            story = selectedStory!!,
            onClose = { viewModel.selectedStory.value = null }
        )
        return
    }

    // 3. If in an active chat -> Chat Detail Screen
    if (activeChatId != null) {
        val activeChat = chats.find { it.id == activeChatId }
        if (activeChat != null) {
            ChatDetailScreen(
                chat = activeChat,
                viewModel = viewModel
            )
            return
        }
    }

    // 4. Main Tabbed Navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = theme.background,
        bottomBar = {
            NavigationBar(
                containerColor = theme.surfaceCard,
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Chats
                NavigationBarItem(
                    selected = currentTab == NavTab.CHATS,
                    onClick = { viewModel.setTab(NavTab.CHATS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                            contentDescription = "Chats"
                        )
                    },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = theme.primaryLight,
                        indicatorColor = theme.primary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Homies
                NavigationBarItem(
                    selected = currentTab == NavTab.HOMIES,
                    onClick = { viewModel.setTab(NavTab.HOMIES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.HOMIES) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = "Homies"
                        )
                    },
                    label = { Text("Homies", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = theme.primaryLight,
                        indicatorColor = theme.primary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Profile
                NavigationBarItem(
                    selected = currentTab == NavTab.PROFILE,
                    onClick = { viewModel.setTab(NavTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = theme.primaryLight,
                        indicatorColor = theme.primary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    NavTab.CHATS, NavTab.STORIES -> ChatListScreen(viewModel = viewModel)
                    NavTab.HOMIES -> HomiesDirectoryScreen(viewModel = viewModel)
                    NavTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Dialogs
    if (showCreateStory) {
        CreateStoryDialog(
            onDismiss = { viewModel.showCreateStoryDialog.value = false },
            onCreateStory = { type, content, uri, voice, bg ->
                viewModel.createStory(type, content, uri, voice, bg)
            }
        )
    }

    if (showCreateGroup) {
        CreateGroupDialog(
            allUsers = allUsers,
            currentUsername = currentUser?.username ?: "",
            onDismiss = { viewModel.showCreateGroupDialog.value = false },
            onCreateGroup = { title, participants ->
                viewModel.createGroup(title, participants)
            }
        )
    }
}
