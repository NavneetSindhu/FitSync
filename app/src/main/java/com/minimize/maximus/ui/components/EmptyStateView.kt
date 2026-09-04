package com.minimize.maximus.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.LocalAccentColor

/**
 * Standardized, reusable Empty State View matching the UniSwap design system.
 * Features optional Lottie animation (or animated vector icon), typography, and optional CTA action button.
 */
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    @RawRes lottieRes: Int? = com.minimize.maximus.R.raw.anim_cat_relaxing,
    fallbackIcon: ImageVector? = null,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null,
    animationSize: Dp = 140.dp
) {
    val currentAccent = LocalAccentColor.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val scaleAnim = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Animation or Fallback Vector Icon
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            },
            contentAlignment = Alignment.Center
        ) {
            if (lottieRes != null && lottieRes != 0) {
                LottieAnimationWrapper(
                    resId = lottieRes,
                    modifier = Modifier.size(animationSize)
                )
            } else if (fallbackIcon != null) {
                Surface(
                    shape = CircleShape,
                    color = currentAccent.copy(alpha = 0.12f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        // 4. Optional CTA Action Button
        if (!ctaText.isNullOrBlank() && onCtaClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = ctaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
