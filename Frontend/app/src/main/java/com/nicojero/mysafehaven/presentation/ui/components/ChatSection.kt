package com.nicojero.mysafehaven.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.HavenViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState

@Composable
fun ChatSection(
    havenId: Int,
    havenViewModel: HavenViewModel,
    authViewModel: AuthViewModel,
    isChatOpen: Boolean,
    onToggleChat: () -> Unit
) {
    val messages by havenViewModel.messages.collectAsState()
    val session by authViewModel.sessionState.collectAsState()
    val myUserId = (session as? SessionState.LoggedIn)?.userId ?: -1

    var text by remember { mutableStateOf("") }

    LaunchedEffect(havenId, isChatOpen) {
        if (isChatOpen) havenViewModel.loadChatMessages(havenId)
    }

    if (!isChatOpen) {
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggleChat,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NUEVO CHAT")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    } else {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chat de la sala",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onToggleChat) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }


                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = false
                ) {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            isMine = message.userId == myUserId
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                havenViewModel.sendMessage(havenId, text)
                                text = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            }
        }
    }
}