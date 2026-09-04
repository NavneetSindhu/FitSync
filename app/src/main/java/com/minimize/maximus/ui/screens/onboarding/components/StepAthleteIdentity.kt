package com.minimize.maximus.ui.screens.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R

@Composable
fun StepAthleteIdentity(
    userName: String,
    onNameChange: (String) -> Unit,
    selectedGender: String,
    onGenderChange: (String) -> Unit,
    accentColor: Color
) {
    Text(
        text = stringResource(R.string.onboarding_step1_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.onboarding_step1_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(20.dp))

    // Centered Avatar Preview (First in column)
    val previewAvatarId = remember(selectedGender) {
        com.minimize.maximus.ui.components.getAvatarForGender(selectedGender)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        com.minimize.maximus.ui.components.MaximusAvatarView(
            avatarId = previewAvatarId,
            size = 88.dp,
            showEditBadge = false
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.onboarding_step1_name_label),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = userName,
        onValueChange = onNameChange,
        placeholder = { Text(stringResource(R.string.onboarding_step1_name_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Person, null, tint = accentColor) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    Text(
        text = stringResource(R.string.onboarding_step1_gender_label),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(10.dp))

    val genders = listOf(
        "Male" to stringResource(R.string.onboarding_gender_male),
        "Female" to stringResource(R.string.onboarding_gender_female),
        "Athlete" to stringResource(R.string.onboarding_gender_athlete)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        genders.forEach { (key, label) ->
            val isSelected = selectedGender == key
            Surface(
                onClick = { onGenderChange(key) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
