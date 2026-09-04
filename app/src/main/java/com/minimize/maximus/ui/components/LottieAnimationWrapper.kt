package com.minimize.maximus.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*

/**
 * Scalable Composable wrapper for Lottie animations matching the UniSwap architecture.
 */
@Composable
fun LottieAnimationWrapper(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true,
    speed: Float = 1f,
    restartOnPlay: Boolean = true
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = restartOnPlay
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}
