// presentation/ui/screens/SearchScreen.kt
package com.nicojero.mysafehaven.presentation.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicojero.mysafehaven.domain.model.Haven
import com.nicojero.mysafehaven.domain.model.Post
import com.nicojero.mysafehaven.presentation.ui.components.ChatBubble
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState
import com.nicojero.mysafehaven.presentation.viewmodel.HavenViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SearchUiState
import com.nicojero.mysafehaven.presentation.viewmodel.SearchViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val havenDetails by viewModel.havenDetails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedHavenTab by rememberSaveable { mutableIntStateOf(0) }
    val havenViewModel: HavenViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Escuchar mensajes de suscripción
    LaunchedEffect(Unit) {
        viewModel.subscriptionMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.checkLocationAndLoadHavens()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState is SearchUiState.Success) {
                FloatingActionButton(
                    onClick = { viewModel.refreshLocation() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar ubicación")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    LoadingView()
                }

                is SearchUiState.LocationDisabled -> {
                    LocationDisabledView(
                        onEnableLocation = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }

                is SearchUiState.Success -> {
                    NearbyHavensContent(
                        havens = state.nearbyHavens,
                        onHavenClick = { haven ->
                            viewModel.loadHavenDetails(haven)
                        },
                        onSubscribe = { havenId ->
                            viewModel.subscribeToHaven(havenId)
                        },
                        onUnsubscribe = { havenId ->
                            viewModel.unsubscribeFromHaven(havenId)
                        }
                    )
                }

                is SearchUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.checkLocationAndLoadHavens() }
                    )
                }
            }

            // Modal de detalles del Haven
            AnimatedVisibility(
                visible = havenDetails.haven != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut()
            ) {
                HavenDetailsModal(
                    searchViewModel = viewModel,
                    selectedTabIndex = selectedHavenTab,
                    onTabSelected = { selectedHavenTab = it },
                    onDismiss = {
                        selectedHavenTab = 0
                        viewModel.clearHavenDetails()
                    },
                    havenViewModel = havenViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Buscando Havens cercanos...")
        }
    }
}

@Composable
private fun LocationDisabledView(onEnableLocation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Ubicación desactivada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Necesitamos tu ubicación para encontrar Havens cercanos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onEnableLocation) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Activar ubicación")
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun NearbyHavensContent(
    havens: List<Haven>,
    onHavenClick: (Haven) -> Unit,
    onSubscribe: (Int) -> Unit,
    onUnsubscribe: (Int) -> Unit
) {
    if (havens.isEmpty()) {
        EmptyHavensView()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Havens cercanos (${havens.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(havens) { haven ->
                HavenCard(
                    haven = haven,
                    onClick = { onHavenClick(haven) },
                    onSubscribe = { onSubscribe(haven.id) },
                    onUnsubscribe = { onUnsubscribe(haven.id) }
                )
            }
        }
    }
}
@Composable
private fun formatDate(date: LocalDateTime?): String {
    if (date == null) return ""
    return try {
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        ""
    }
}

@Composable
private fun EmptyHavensView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationSearching,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No hay Havens cercanos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No encontramos ningún Haven en tu ubicación actual",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HavenCard(
    haven: Haven,
    onClick: () -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = haven.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Por ${haven.ownerUsername ?: "Usuario"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (haven.isSubscribed) {
                    FilledIconButton(
                        onClick = onUnsubscribe,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Desuscribirse")
                    }
                } else {
                    FilledIconButton(
                        onClick = onSubscribe,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Suscribirse")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip2(
                    icon = Icons.Default.Place,
                    text = "${haven.distanceMeters?.toInt() ?: 0}m"
                )
                InfoChip2(
                    icon = Icons.Default.Star,
                    text = "Radio: ${haven.radius.toInt()}m"
                )
            }

            if (haven.isSubscribed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Suscrito - Toca para ver posts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip2(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HavenDetailsModal(
    searchViewModel: SearchViewModel,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    havenViewModel: HavenViewModel,
    authViewModel: AuthViewModel
) {
    val havenDetails by searchViewModel.havenDetails.collectAsState()

    val haven = havenDetails.haven ?: return
    val havenId = haven.id
    val isUserSubscribed = haven.isSubscribed

    val tabs = listOf("Posts", "Chat Comunitario")

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- SCRIM ---------- */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismiss()
                }
        )

        /* ---------- MODAL ---------- */
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .pointerInput(Unit) {}, // 👈 evita robo de eventos
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                /* ---------- HEADER ---------- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = haven.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                /* ---------- TABS ---------- */
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = { Text(title) }
                        )
                    }
                }

                /* ---------- CONTENT ---------- */
                when (selectedTabIndex) {
                    0 -> PostsTabContent(havenDetails)
                    1 -> ChatTabContent(
                        havenId = havenId,
                        havenViewModel = havenViewModel,
                        authViewModel = authViewModel,
                        isUserSubscribed = isUserSubscribed
                    )
                }
            }
        }
    }
}

@Composable
private fun PostItem(post: Post) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = "Post icon",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = post.content, // <-- Display the 'content' property of the Post
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PostCard2(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDate(post.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostsTabContent(havenDetails: HavenDetailsState) {
    val contentModifier = Modifier.fillMaxSize()

    when {
        havenDetails.isLoading -> {
            Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        havenDetails.error != null -> {
            Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                Text(text = havenDetails.error, color = MaterialTheme.colorScheme.error)
            }
        }
        havenDetails.posts.isEmpty() -> {
            Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                Text("No hay posts en este Haven")
            }
        }
        else -> {
            LazyColumn(
                modifier = contentModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(havenDetails.posts) { post ->
                    PostCard2(post)
                }
            }
        }
    }
}

@Composable
private fun ChatTabContent (
    havenId: Int,
    havenViewModel: HavenViewModel,
    authViewModel: AuthViewModel,
    isUserSubscribed: Boolean
) {
    val messages by havenViewModel.messages.collectAsState()
    val session by authViewModel.sessionState.collectAsState()
    val myUserId = (session as? SessionState.LoggedIn)?.userId ?: -1

    var text by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(havenId, isUserSubscribed) {
        if (isUserSubscribed) {
            havenViewModel.loadChatMessages(havenId)
        }
    }

    if (!isUserSubscribed) {
        Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Icon (
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text (
                    text = "Suscríbete al Haven para acceder al chat comunitario.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        Column (modifier = Modifier.fillMaxSize()) {
            LazyColumn (
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true,
                state = listState
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        isMine = message.userId == myUserId
                    )
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField (
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    enabled = isUserSubscribed
                )
                IconButton (
                    onClick = {
                        if (text.isNotBlank()) {
                            havenViewModel.sendMessage(havenId, text)
                            text = ""
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon (
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar"
                    )
                }
            }
            LaunchedEffect(messages) {
                if (messages.isNotEmpty()) {
                    listState.scrollToItem(0)
                }
            }
        }
    }
}