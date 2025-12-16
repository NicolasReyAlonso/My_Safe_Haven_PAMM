package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicojero.mysafehaven.presentation.ui.components.ChatSection
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.HavenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    havenId: Int,
    havenViewModel: HavenViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(havenId) {
        havenViewModel.loadChatMessages(havenId)
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Chat del Haven") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        ChatSection(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            havenId = havenId,
            havenViewModel = havenViewModel,
            authViewModel = authViewModel,
            isChatOpen = true,
            onToggleChat = {}
        )
    }
}