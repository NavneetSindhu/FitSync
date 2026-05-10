package com.example.fitsync.ui.screens.chat

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // NEW: Coil import
import com.example.fitsync.domain.model.chat.ChatMessage
import com.example.fitsync.domain.model.chat.Macros
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String = "Navneet",
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            selectedImageBitmap = bitmap
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = { ChatHeader(userName, onBackClick,onClearChatClick = { viewModel.clearTodayChat() })},
        bottomBar = {
            ChatInputBar(
                text = messageText,
                stagedImage = selectedImageBitmap,
                onTextChange = { messageText = it },
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = { selectedImageBitmap = null },
                onSend = {
                    if (selectedImageBitmap != null) {
                        viewModel.analyzeMealImage(selectedImageBitmap!!, messageText)
                        selectedImageBitmap = null
                        messageText = ""
                    } else if (messageText.isNotBlank()) {
                        viewModel.sendTextMessage(messageText)
                        messageText = ""
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                if (msg.sentByUser) {
                    UserMessageBubble(msg)
                } else {
                    BotMessageBubble(
                        message = msg,
                        onActionClick = { actionText ->
                            viewModel.sendTextMessage(actionText)
                        }
                    )
                }
            }

            if (isTyping) {
                item { TypingIndicator() }
            }
        }

        LaunchedEffect(messages.size, isTyping) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
        }
    }
}

// --- Components ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    userName: String,
    onBackClick: () -> Unit,
    onClearChatClick: () -> Unit // NEW: Pass the click event up
) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d"))

    // NEW: State to manage the dropdown menu visibility
    var expanded by remember { mutableStateOf(false) }

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
                // --- NEW: The 3-Dot Options Menu ---
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                expanded = false
                                onClearChatClick()
                            }
                        )
                        // You can easily add more DropdownMenuItems here later!
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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

            // CHANGED: Check for imageLocalPath instead of raw Bitmap
            if (message.imageLocalPath != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // CHANGED: Use Coil to safely and asynchronously load the image from disk
                    AsyncImage(
                        model = message.imageLocalPath,
                        contentDescription = "User Meal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            if (message.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
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
fun BotMessageBubble(message: ChatMessage, onActionClick: (String) -> Unit) {
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

                if (message.text.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                MealAnalysisCard(macros = message.macros)

                Spacer(Modifier.height(8.dp))
                ActionPillsRow(
                    actions = listOf("Log this meal", "Suggest a lighter dinner"),
                    onClick = onActionClick
                )

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

                if (message.text.contains("don't see any food", ignoreCase = true)) {
                    Spacer(Modifier.height(8.dp))
                    ActionPillsRow(
                        actions = listOf("Try taking a new photo", "Enter macros manually"),
                        onClick = onActionClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionPillsRow(actions: List<String>, onClick: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        actions.forEach { action ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable { onClick(action) }
            ) {
                Text(
                    text = action,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MealAnalysisCard(macros: Macros) {
    Card(
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(macros.mealName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Portion: ${macros.estimatedQuantity}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text("🔥 ${macros.calories} kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(16.dp))

            MacroRow("Protein", macros.protein, 50, Color(0xFFE53935))
            Spacer(Modifier.height(8.dp))
            MacroRow("Carbs", macros.carbs, 50, Color(0xFF1E88E5))
            Spacer(Modifier.height(8.dp))
            MacroRow("Fat", macros.fat, 30, Color(0xFFFFB300))
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
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("Sync AI is thinking...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun ChatInputBar(
    text: String,
    stagedImage: Bitmap?,
    onTextChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp, // Keeps the nice floating drop-shadow
        shape = RoundedCornerShape(28.dp), // 1. Rounds the entire outer container
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // 3. The safe margin floating effect
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // --- Staged Image Preview Area ---
            if (stagedImage != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                        .size(80.dp)
                ) {
                    Image(
                        bitmap = stagedImage.asImageBitmap(),
                        contentDescription = "Staged Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // --- Existing Input Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp), // Slightly reduced inner padding to fit the pill shape better
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onImageClick) {
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

                val canSend = text.isNotBlank() || stagedImage != null

                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier
                        .background(
                            if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}