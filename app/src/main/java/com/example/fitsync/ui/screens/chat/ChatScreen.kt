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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fitsync.domain.model.chat.ChatMessage
import com.example.fitsync.domain.model.chat.Macros
import com.example.fitsync.ui.components.shimmer
import com.example.fitsync.ui.theme.LocalAccentColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String = "Athlete",
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Theme & Preference injection
    val currentAccent = LocalAccentColor.current
    val isDark = isSystemInDarkTheme()

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
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            selectedImageBitmap = bitmap
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            ChatHeader(
                userName = userName,
                onBackClick = onBackClick,
                accentColor = currentAccent,
                onClearChatClick = { viewModel.clearTodayChat() }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                stagedImage = selectedImageBitmap,
                onTextChange = { messageText = it },
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = { selectedImageBitmap = null },
                accentColor = currentAccent,
                isDarkTheme = isDark,
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
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                if (msg.sentByUser) {
                    UserMessageBubble(msg, currentAccent)
                } else {
                    BotMessageBubble(
                        message = msg,
                        accentColor = currentAccent,
                        onActionClick = { actionText -> viewModel.sendTextMessage(actionText) }
                    )
                }
            }

            if (isTyping) {
                item { AIShimmerPlaceholder(accentColor = currentAccent)}
            }
        }

        LaunchedEffect(messages.size, isTyping) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    userName: String,
    onBackClick: () -> Unit,
    onClearChatClick: () -> Unit,
    accentColor: Color
) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d"))
    var expanded by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.padding(6.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Sync Nutrition AI", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text("Today, $currentDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, "Menu")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Clear Conversation") },
                        leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onClearChatClick()
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.1f))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$userName! Let's make every meal count. 🥗",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun UserMessageBubble(message: ChatMessage, accentColor: Color) {
    val contentColor = if (accentColor.luminance() > 0.45f) Color.Black else Color.White
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth(0.8f)) {
            if (message.imageLocalPath != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    AsyncImage(
                        model = message.imageLocalPath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
            if (message.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    color = accentColor
                ) {
                    Text(
                        text = message.text,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BotMessageBubble(message: ChatMessage, accentColor: Color, onActionClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = accentColor.copy(alpha = 0.15f)) {
            Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.padding(4.dp))
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                MealAnalysisCard(macros = message.macros, accentColor = accentColor)
                Spacer(Modifier.height(8.dp))
                ActionPillsRow(actions = listOf("Log this meal", "Suggest lighter dinner"), accentColor = accentColor, onClick = onActionClick)
            } else {
                Surface(shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionPillsRow(actions: List<String>, accentColor: Color, onClick: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.clickable { onClick(action) }
            ) {
                Text(
                    text = action,
                    color = accentColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MealAnalysisCard(macros: Macros, accentColor: Color) {
    Card(
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(macros.mealName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text("Portion: ${macros.estimatedQuantity}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("🔥 ${macros.calories} kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))
            MacroRow("Protein", macros.protein, 50, Color(0xFFE53935))
            Spacer(Modifier.height(8.dp))
            MacroRow("Carbs", macros.carbs, 60, Color(0xFF1E88E5))
            Spacer(Modifier.height(8.dp))
            MacroRow("Fat", macros.fat, 20, Color(0xFFFFB300))
        }
    }
}

@Composable
fun MacroRow(label: String, amount: Int, target: Int, color: Color) {
    val progress = (amount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(55.dp), style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(progress = { progress }, color = color, modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape))
        Text("${amount}g", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TypingIndicator(accentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accentColor)
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
    onSend: () -> Unit,
    accentColor: Color,
    isDarkTheme: Boolean
) {
    // 🔥 Improved Color Logic: Using specific hex for Dark Mode to prevent "White Box" issue
    val backgroundColor = if (isDarkTheme) {
        Color(0xFF1C1C1E).copy(alpha = 0.85f) // Deep dark gray for DM
    } else {
        Color(0xFFF2F2F7).copy(alpha = 0.90f) // Light gray for LM
    }

    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.10f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(32.dp))
    ) {
        // Blur Layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { renderEffect = BlurEffect(30f, 30f, TileMode.Clamp) }
            )
        }

        Column(modifier = Modifier.padding(8.dp)) {
            // Image Preview (if any)
            if (stagedImage != null) {
                Box(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp).size(70.dp)) {
                    Image(
                        bitmap = stagedImage.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                    Surface(
                        onClick = onRemoveImage,
                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).offset(x = 4.dp, y = (-4).dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onImageClick) {
                    Icon(Icons.Default.AddAPhoto, null, tint = accentColor)
                }

                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            "Track meal or ask AI...",
                            fontSize = 14.sp,
                            color = if (isDarkTheme) Color.White.copy(0.4f) else Color.Black.copy(0.4f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDarkTheme) Color.White else Color.Black
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,

                        // Remove the underline
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        cursorColor = accentColor
                    ),
                    maxLines = 4
                )

                val canSend = text.isNotBlank() || stagedImage != null
                val sendButtonColor = if (canSend) accentColor else (if (isDarkTheme) Color.White.copy(0.1f) else Color.Black.copy(0.05f))
                val iconColor = if (canSend) {
                    if (accentColor.luminance() > 0.45f) Color.Black else Color.White
                } else {
                    if (isDarkTheme) Color.White.copy(0.3f) else Color.Black.copy(0.3f)
                }

                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier
                        .size(42.dp)
                        .background(sendButtonColor, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp).padding(start = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AIShimmerPlaceholder(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Mock Avatar Circle matching BotMessageBubble structure
        Surface(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .shimmer(accentColor),
            color = Color.Transparent
        ) {}

        Spacer(Modifier.width(8.dp))

        // Mock Text blocks matching Chat Surface look
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                    .shimmer(accentColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                    .shimmer(accentColor)
            )
        }
    }
}