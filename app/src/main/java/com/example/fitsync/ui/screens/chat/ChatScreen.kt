package com.example.fitsync.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.R // Replace with your actual R class
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- Data Models ---
data class Macros(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val isAnalysis: Boolean = false,
    val mealName: String? = null,
    val macros: Macros? = null,
    val imageRes: Int? = null // Using drawable for mock, replace with URI/Bitmap later
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String = "Navneet",
    onBackClick: () -> Unit = {}
) {
    // Mock Data for the UI
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage("1", "Hi $userName! I'm Sync AI. Ready to analyze your meals?", isUser = false),
            ChatMessage("2", "Here is my lunch!", isUser = true, imageRes = android.R.drawable.ic_menu_camera), // Placeholder
            ChatMessage(
                id = "3",
                text = "Great choice! Here is the breakdown.",
                isUser = false,
                isAnalysis = true,
                mealName = "Chicken Quinoa Bowl",
                macros = Macros(450, 35, 25, 15)
            )
        )
    }

    Scaffold(
        topBar = { ChatHeader(userName, onBackClick) },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        messages.add(ChatMessage(System.currentTimeMillis().toString(), messageText, isUser = true))
                        messageText = ""
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                if (msg.isUser) {
                    UserMessageBubble(msg)
                } else {
                    BotMessageBubble(msg)
                }
            }
        }

        // Auto-scroll to bottom on new message
        LaunchedEffect(messages.size) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
}

// --- Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(userName: String, onBackClick: () -> Unit) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d"))

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Sync Nutrition AI", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Today, $currentDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
            // Welcome Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$userName! Let's make every meal count. 🥗",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth(0.8f)) {
            if (message.imageRes != null) {
                // Image Bubble
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Box(modifier = Modifier.size(200.dp).background(Color.LightGray)) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.Gray)
                        // TODO: Replace with AsyncImage when using actual URIs
                    }
                }
            }
            if (message.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    // Use your LocalAccentColor.current here if you set it up!
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BotMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(4.dp)
            )
        }
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            if (message.isAnalysis && message.macros != null) {
                MealAnalysisCard(message.mealName ?: "Unknown Meal", message.macros)
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MealAnalysisCard(mealName: String, macros: Macros) {
    Card(
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(mealName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Text("🔥 ${macros.calories} kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(16.dp))

            // Macro Bars
            MacroRow("Protein", macros.protein, 50, Color(0xFFE53935))
            Spacer(Modifier.height(8.dp))
            MacroRow("Carbs", macros.carbs, 50, Color(0xFF1E88E5))
            Spacer(Modifier.height(8.dp))
            MacroRow("Fat", macros.fat, 30, Color(0xFFFFB300))

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐️ Good Choice!", color = Color(0xFF43A047), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun MacroRow(label: String, amount: Int, dailyTarget: Int, color: Color) {
    val progress = (amount.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text("${amount}g", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Open Camera */ }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { /* Open Gallery */ }) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Describe your meal...", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 3
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .background(
                        if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp) // Optical center tweak
                )
            }
        }
    }
}