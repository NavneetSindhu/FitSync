package com.minimize.maximus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimize.maximus.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch

/**
 * CompositionLocal providing an animated slide-down dismissal trigger for any child inside MaximusModalBottomSheet.
 */
val LocalBottomSheetDismiss = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Centralized, reusable Bottom Sheet component following standard Material 3 architecture.
 * Space-efficient: Header and close icon are integrated on the same row with zero wasted top space.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaximusModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val dismissWithAnimation: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        dragHandle = dragHandle,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        CompositionLocalProvider(LocalBottomSheetDismiss provides dismissWithAnimation) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                content()
            }
        }
    }
}

/**
 * Reusable space-efficient Header for bottom sheets with title, optional subtitle,
 * and integrated circular close button on the exact same row.
 */
@Composable
fun MaximusSheetHeader(
    title: String,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dismissAction = onDismiss ?: LocalBottomSheetDismiss.current
    val currentAccent = LocalAccentColor.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = currentAccent
                )
            }
        }

        IconButton(
            onClick = dismissAction,
            modifier = Modifier.size(36.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sheet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
