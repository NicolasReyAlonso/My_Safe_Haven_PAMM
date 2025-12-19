package com.nicojero.mysafehaven.presentation.ui.screens
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.domain.model.Haven
import com.nicojero.mysafehaven.domain.model.Post
import com.nicojero.mysafehaven.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HavenList(
    havens: List<Haven>,
    onOpenChat: (Int) -> Unit,
    onOpenDetails: (Haven) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(havens) { haven ->
            HavenCard(
                haven = haven,
                onClick = { onOpenDetails(haven) },
                onSubscribe = {},
                onUnsubscribe = {},
                onOpenChat = { onOpenChat(haven.id) }
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 ¿Qué es un Haven?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Un Haven es una zona geográfica segura donde puedes compartir contenido y chatear con personas cercanas.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenChat: (Int) -> Unit
) {
    val havens by viewModel.uiState.collectAsState()

    // Estado local para el modal
    var selectedHaven by remember { mutableStateOf<Haven?>(null) }
    var havenDetails by remember { mutableStateOf<com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState>(
        com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState()
    ) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Safe Haven",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Bienvenido a tu espacio seguro",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (havens.isEmpty()) {
            Text(
                text = "Aún no estás suscrito a ningún Haven",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            HavenList(
                havens = havens,
                onOpenChat = onOpenChat,
                onOpenDetails = { haven ->
                    selectedHaven = haven
                    havenDetails = havenDetails.copy(isLoading = true, haven = haven)

                    // Cargar posts
                    viewModel.viewModelScope.launch {
                        when(val result = viewModel.havenRepository.getPosts(haven.id)) {
                            is com.nicojero.mysafehaven.data.repository.HavenResult.Success -> {
                                havenDetails = havenDetails.copy(posts = result.data, isLoading = false)
                            }
                            is com.nicojero.mysafehaven.data.repository.HavenResult.Error -> {
                                havenDetails = havenDetails.copy(error = result.message, isLoading = false)
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard()
    }

    // Mostrar modal si hay haven seleccionado
    selectedHaven?.let {
        HavenDetailsModal(
            havenDetails = havenDetails,
            onDismiss = { selectedHaven = null }
        )
    }
}

@Composable
private fun HavenDetailsModal(
    havenDetails: com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = havenDetails.haven?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    when {
                        havenDetails.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        havenDetails.error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = havenDetails.error,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        havenDetails.posts.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay posts en este Haven")
                            }
                        }

                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(havenDetails.posts) { post ->
                                    PostCard3(post)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PostCard3(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del post
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Usuario",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.getRelativeTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido del post
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )

            // ✅ NUEVO: Mostrar imagen si existe
            post.imagePath?.let { imagePath ->
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    // Construir la URL completa de la imagen
                    val imageUrl = "http://10.0.2.2:5050/$imagePath" // Para emulador
                    // val imageUrl = "http://TU_IP:5050/$imagePath" // Para dispositivo físico

                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(
                            model = imageUrl,
                            error = rememberVectorPainter(Icons.Default.Place)
                        ),
                        contentDescription = "Post image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
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
