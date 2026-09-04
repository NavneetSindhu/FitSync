package com.minimize.maximus.ui.screens.chat

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.minimize.maximus.domain.model.chat.ChatMessage
import com.minimize.maximus.domain.model.chat.Macros
import com.minimize.maximus.ui.components.shimmer
import com.minimize.maximus.ui.theme.LocalAccentColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.minimize.maximus.R
import androidx.compose.ui.res.stringResource
import com.minimize.maximus.ui.components.MaximusConfirmDialog
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String = "Athlete",
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onOpenLibrary: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentAccent = LocalAccentColor.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val uiState by viewModel.uiState.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val listState = rememberLazyListState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

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

    if (showClearConfirmDialog) {
        MaximusConfirmDialog(
            title = stringResource(R.string.chat_clear_dialog_title),
            message = stringResource(R.string.chat_clear_dialog_message),
            confirmText = stringResource(R.string.action_clear),
            dismissText = stringResource(R.string.action_cancel),
            icon = Icons.Default.DeleteSweep,
            isDestructive = true,
            onConfirm = {
                viewModel.clearTodayChat()
                showClearConfirmDialog = false
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            ChatHeader(
                userName = userName,
                onBackClick = onBackClick,
                accentColor = currentAccent,
                onClearChatClick = { showClearConfirmDialog = true }
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = currentAccent.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Maximus AI Coach",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Ask anything about training, progressive overload, splits, or nutrition",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Starter Suggestion Chips
                        val starterPrompts = listOf(
                            "⚡ Analyze my weekly training volume",
                            "🏋️ Suggest a 4-day Upper/Lower split",
                            "🔥 How to break through a bench plateau?",
                            "🥗 What are the best post-workout meals?"
                        )

                        starterPrompts.forEach { prompt ->
                            Surface(
                                onClick = { viewModel.sendTextMessage(prompt.substringAfter(" ")) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(uiState.messages) { msg ->
                if (msg.sentByUser) {
                    UserMessageBubble(
                        message = msg,
                        accentColor = currentAccent,
                        onEditPrompt = { prompt -> messageText = prompt },
                        onDelete = { viewModel.deleteMessage(msg.id) }
                    )
                } else {
                    BotMessageBubble(
                        message = msg,
                        accentColor = currentAccent,
                        onActionClick = { actionText -> viewModel.sendTextMessage(actionText) },
                        onSaveRoutineClick = { routine -> viewModel.saveGeneratedRoutine(msg.id, routine) },
                        onDelete = { viewModel.deleteMessage(msg.id) }
                    )
                }
            }

            if (uiState.isTyping) {
                item { AIShimmerPlaceholder(accentColor = currentAccent)}
            }
        }

        LaunchedEffect(uiState) {
            val itemCount = uiState.messages.size + if (uiState.isTyping) 1 else 0
            if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
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
                        Text(stringResource(R.string.chat_title), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text("Today, $currentDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_cancel))
                }
            },
            actions = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, "Menu")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.chat_clear_dialog_title)) },
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.chat_banner_tagline, userName),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    accentColor: Color,
    onEditPrompt: (String) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val contentColor = if (accentColor.luminance() > 0.45f) Color.Black else Color.White
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val toastManager = LocalMaximusToast.current
    val copiedText = stringResource(R.string.chat_message_copied)

    var showMenu by remember { mutableStateOf(false) }

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
                val bubbleShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                Box {
                    Surface(
                        shape = bubbleShape,
                        color = accentColor,
                        modifier = Modifier
                            .clip(bubbleShape)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            )
                    ) {
                        Text(
                            text = message.text,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))
                    ) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chat_action_copy)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    toastManager.showToast(
                                        message = copiedText,
                                        type = ToastType.SUCCESS,
                                        title = "Copied"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chat_action_edit)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onEditPrompt(message.text)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chat_action_delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BotMessageBubble(
    message: ChatMessage,
    accentColor: Color,
    onActionClick: (String) -> Unit,
    onSaveRoutineClick: (WorkoutRoutine) -> Unit = {},
    onExerciseClick: (String) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val toastManager = LocalMaximusToast.current
    val copiedText = stringResource(R.string.chat_message_copied)

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = accentColor.copy(alpha = 0.15f)) {
            Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.padding(4.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth(0.92f)) {
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val bubbleBorder = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDarkTheme) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            if (message.text.isNotBlank()) {
                val bubbleShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                Box {
                    Surface(
                        shape = bubbleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = bubbleBorder,
                        modifier = Modifier
                            .padding(bottom = if (message.routinePayload != null || (message.isAnalysis && message.macros != null) || message.suggestedReplies.isNotEmpty()) 4.dp else 0.dp)
                            .clip(bubbleShape)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            )
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            com.minimize.maximus.ui.screens.chat.components.RichMarkdownMessage(
                                content = message.text,
                                accentColor = accentColor
                            )
                        }
                    }

                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))
                    ) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chat_action_copy)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    toastManager.showToast(
                                        message = copiedText,
                                        type = ToastType.SUCCESS,
                                        title = "Copied"
                                    )
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chat_action_delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                // Gemini-style bottom copy button
                var isCopied by remember { mutableStateOf(false) }
                LaunchedEffect(isCopied) {
                    if (isCopied) {
                        kotlinx.coroutines.delay(2000L)
                        isCopied = false
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 2.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            clipboardManager.setText(AnnotatedString(message.text))
                            isCopied = true
                            toastManager.showToast(
                                message = copiedText,
                                type = ToastType.SUCCESS,
                                title = "Copied"
                            )
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        AnimatedContent(
                            targetState = isCopied,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "CopyIconAnimation"
                        ) { copied ->
                            if (copied) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Copied",
                                    tint = accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (message.routinePayload != null) {
                RoutinePreviewCard(
                    routine = message.routinePayload,
                    isSaved = message.isRoutineSaved,
                    accentColor = accentColor,
                    onSaveClick = { onSaveRoutineClick(message.routinePayload) },
                    onExerciseClick = onExerciseClick
                )
            } else if (message.isAnalysis && message.macros != null) {
                MealAnalysisCard(macros = message.macros, accentColor = accentColor)
                Spacer(Modifier.height(8.dp))
                ActionPillsRow(actions = listOf("Log this meal", "Suggest lighter dinner"), accentColor = accentColor, onClick = onActionClick)
            }

            // Phase 3: Dynamic Suggested Follow-up Quick-Reply Chips
            if (message.suggestedReplies.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                ActionPillsRow(
                    actions = message.suggestedReplies,
                    accentColor = accentColor,
                    onClick = onActionClick
                )
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
fun RoutinePreviewCard(
    routine: WorkoutRoutine,
    isSaved: Boolean,
    accentColor: Color,
    onSaveClick: () -> Unit,
    onExerciseClick: (String) -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorder = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (routine.description.isNotBlank()) {
                            Text(
                                text = routine.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "~${routine.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            // Exercise items preview with deep linking
            routine.exercises.forEach { ex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onExerciseClick(ex.exerciseName) }
                        .padding(vertical = 5.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ExerciseIcon(
                            name = ex.exerciseName,
                            size = 32.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = ex.exerciseName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            val weightText = if (ex.defaultWeight > 0f) " @ ${ex.defaultWeight}kg" else ""
                            Text(
                                text = "${ex.targetSets} × ${ex.targetReps}$weightText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View in Library",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // CTA Button with Haptic feedback and confirmation
            val haptic = LocalHapticFeedback.current
            val toastManager = LocalMaximusToast.current

            Button(
                onClick = {
                    if (!isSaved) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveClick()
                        toastManager.showToast(
                            message = "Routine \"${routine.name}\" saved to My Routines!",
                            type = ToastType.SUCCESS,
                            title = "Routine Saved"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaved,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) MaterialTheme.colorScheme.surfaceVariant else accentColor,
                    contentColor = if (isSaved) MaterialTheme.colorScheme.onSurfaceVariant else if (accentColor.luminance() > 0.45f) Color.Black else Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "Saved to My Routines" else "Save as Routine",
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
    val backgroundColor = if (isDarkTheme) {
        Color(0xFF1E1E22)
    } else {
        Color(0xFFF4F4F6)
    }

    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.12f)
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
                            stringResource(R.string.chat_input_placeholder),
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