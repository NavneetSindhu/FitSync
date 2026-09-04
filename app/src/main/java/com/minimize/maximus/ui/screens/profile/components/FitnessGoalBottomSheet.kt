package com.minimize.maximus.ui.screens.profile.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.screens.onboarding.model.GoalOption
import com.minimize.maximus.ui.screens.onboarding.model.OnboardingGoals
import com.minimize.maximus.ui.theme.LocalAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessGoalBottomSheet(
    currentGoal: String,
    onGoalSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    var selectedGoalKey by remember { mutableStateOf(currentGoal) }

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        MaximusSheetHeader(
            title = "Fitness Goal",
            subtitle = "Select your primary training focus"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingGoals.forEach { goalOption ->
                val isSelected = selectedGoalKey.equals(goalOption.goalKey, ignoreCase = true) ||
                        currentGoal.equals(goalOption.goalKey, ignoreCase = true) && selectedGoalKey == currentGoal

                GoalSelectionCard(
                    goal = goalOption,
                    isSelected = selectedGoalKey == goalOption.goalKey,
                    accentColor = currentAccent,
                    onSelect = {
                        selectedGoalKey = goalOption.goalKey
                        onGoalSelected(goalOption.goalKey)
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalSelectionCard(
    goal: GoalOption,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "GoalCardScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Emoji badge
            Surface(
                shape = CircleShape,
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = goal.emoji, fontSize = 22.sp)
                }
            }

            // Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(goal.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(goal.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            // Selection Check Circle
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = accentColor,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
