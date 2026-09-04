package com.minimize.maximus.ui.screens.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.screens.onboarding.model.OnboardingGoals

@Composable
fun StepFitnessGoal(
    selectedGoalKey: String,
    onGoalSelected: (String) -> Unit,
    accentColor: Color
) {
    Text(
        text = stringResource(R.string.onboarding_step3_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.onboarding_step3_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(20.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OnboardingGoals.forEach { goal ->
            val isSelected = selectedGoalKey == goal.goalKey

            Surface(
                onClick = { onGoalSelected(goal.goalKey) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = goal.emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(16.dp))
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
