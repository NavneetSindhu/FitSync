package com.minimize.maximus.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.maximus.R
import com.minimize.maximus.ui.theme.LocalAccentColor

data class MaximusAvatar(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val drawableRes: Int,
    val gradientColors: List<Color>
)

val MaximusAvatars = listOf(
    MaximusAvatar(
        id = "avatar_male",
        title = "Male Athlete",
        subtitle = "Strength & Speed",
        drawableRes = R.drawable.avatar_male,
        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA))
    ),
    MaximusAvatar(
        id = "avatar_female",
        title = "Female Athlete",
        subtitle = "Power & Agility",
        drawableRes = R.drawable.avatar_female,
        gradientColors = listOf(Color(0xFFDB2777), Color(0xFFF472B6))
    ),
    MaximusAvatar(
        id = "avatar_beast",
        title = "Beast Mode",
        subtitle = "Iron & Grit",
        drawableRes = R.drawable.avatar_beast,
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))
    ),
    MaximusAvatar(
        id = "avatar_champion",
        title = "Champion",
        subtitle = "Gold Standard",
        drawableRes = R.drawable.avatar_champion,
        gradientColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24))
    ),
    MaximusAvatar(
        id = "avatar_runner",
        title = "Speed Runner",
        subtitle = "Cardio & Stamina",
        drawableRes = R.drawable.avatar_runner,
        gradientColors = listOf(Color(0xFF059669), Color(0xFF34D399))
    )
)

fun getAvatarById(id: String): MaximusAvatar {
    return MaximusAvatars.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MaximusAvatars[0]
}

fun getAvatarForGender(gender: String): String {
    return when (gender.lowercase()) {
        "female" -> "avatar_female"
        "male" -> "avatar_male"
        else -> "avatar_beast"
    }
}

/**
 * Centered Uniswap-style Profile Avatar View
 */
@Composable
fun MaximusAvatarView(
    avatarId: String,
    size: Dp = 96.dp,
    modifier: Modifier = Modifier,
    showEditBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val currentAccent = LocalAccentColor.current
    val avatar = remember(avatarId) { getAvatarById(avatarId) }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Shadow & Vector Image Ring
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = avatar.gradientColors.first().copy(alpha = 0.4f),
                    spotColor = avatar.gradientColors.last().copy(alpha = 0.5f)
                ),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatar.drawableRes),
                    contentDescription = avatar.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Edit Pencil Badge
        if (showEditBadge && onClick != null) {
            Surface(
                shape = CircleShape,
                color = currentAccent,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Avatar",
                        tint = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
