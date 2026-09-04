package com.minimize.maximus.ui.screens.log.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.EmptyStateView

@Composable
fun EmptyWorkoutState(
    modifier: Modifier = Modifier,
    onAddExerciseClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateView(
            title = stringResource(R.string.log_empty_workout_title),
            subtitle = stringResource(R.string.log_empty_workout_desc),
            fallbackIcon = Icons.Default.FitnessCenter,
            ctaText = if (onAddExerciseClick != null) stringResource(R.string.log_add_exercise) else null,
            onCtaClick = onAddExerciseClick
        )
    }
}
