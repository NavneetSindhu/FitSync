package com.minimize.maximus.ui.screens.profile.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.minimize.maximus.ui.components.MaximusAvatars
import com.minimize.maximus.ui.components.getAvatarById
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * In-place Inline Avatar Selector with overlapping crescent 3D paging effect.
 */
@Composable
fun InlineAvatarSelector(
    currentAvatarId: String?,
    isEditing: Boolean,
    onEditChange: (Boolean) -> Unit,
    onAvatarSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val avatars = MaximusAvatars
    val initialIndex = remember(currentAvatarId) {
        val idx = avatars.indexOfFirst { it.id == currentAvatarId }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { avatars.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Whenever edit mode is entered, reset/scroll to currentAvatarId
    LaunchedEffect(isEditing, currentAvatarId) {
        val idx = avatars.indexOfFirst { it.id == currentAvatarId }
        val targetIdx = if (idx >= 0) idx else 0
        if (pagerState.currentPage != targetIdx) {
            pagerState.scrollToPage(targetIdx)
        }
    }

    val selectedAvatar = avatars[pagerState.currentPage.coerceIn(0, avatars.lastIndex)]

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            contentAlignment = Alignment.Center
        ) {
            val itemSize = 100.dp
            val horizontalPadding = (maxWidth - itemSize) / 2

            if (isEditing) {
                // Interactive Overlapping Crescent 3D Pager
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = (-30).dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val avatar = avatars[page]
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    val offsetAbs = pageOffset.absoluteValue
                    val isCenter = offsetAbs < 0.2f

                    val scale = (1f - (offsetAbs * 0.18f)).coerceIn(0.76f, 1f)
                    val alpha = (1f - (offsetAbs * 0.22f)).coerceIn(0.55f, 1f)
                    val zIndexValue = (10f - offsetAbs * 4f).coerceAtLeast(0f)

                    Box(
                        modifier = Modifier
                            .size(itemSize)
                            .zIndex(zIndexValue)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isCenter) {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = avatar.drawableRes),
                            contentDescription = avatar.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .then(
                                    if (isCenter) {
                                        Modifier
                                            .border(3.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                                            .border(2.5.dp, currentAccent, CircleShape)
                                    } else {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                    }
                                )
                        )
                    }
                }
            } else {
                // Normal Single Centered Profile Avatar
                val currentAvatar = remember(currentAvatarId) { getAvatarById(currentAvatarId ?: "avatar_male") }
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            onEditChange(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = currentAvatar.gradientColors.first().copy(alpha = 0.4f),
                                spotColor = currentAvatar.gradientColors.last().copy(alpha = 0.5f)
                            ),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Image(
                            painter = painterResource(id = currentAvatar.drawableRes),
                            contentDescription = currentAvatar.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        )
                    }
                }
            }

            // Morphing Edit Pencil <-> Save Checkmark Button (pinned to center avatar bottom-right)
            Box(
                modifier = Modifier
                    .size(itemSize)
                    .zIndex(20f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(6.dp, CircleShape)
                        .clickable {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            if (isEditing) {
                                onAvatarSaved(selectedAvatar.id)
                                onEditChange(false)
                            } else {
                                onEditChange(true)
                            }
                        },
                    shape = CircleShape,
                    color = if (isEditing) currentAccent else MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Crossfade(
                            targetState = isEditing,
                            animationSpec = tween(220),
                            label = "avatar_button_icon"
                        ) { editing ->
                            if (editing) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save Avatar",
                                    tint = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = currentAccent,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // When in editing mode, show the selected avatar name tag inline
        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = currentAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${selectedAvatar.title} • ${selectedAvatar.subtitle}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = currentAccent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
