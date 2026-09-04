package com.minimize.maximus.ui.screens.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R

@Composable
fun OnboardingBottomBar(
    isFinalStep: Boolean,
    accentColor: Color,
    onContinueClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onContinueClick,
            shape = com.minimize.maximus.ui.theme.MaximusShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (accentColor.luminance() > 0.45f) Color.Black else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isFinalStep) {
                        stringResource(R.string.onboarding_finish)
                    } else {
                        stringResource(R.string.onboarding_continue)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (isFinalStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
