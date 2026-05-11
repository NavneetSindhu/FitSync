//package com.example.fitsync.ui.screens.auth
//
//import androidx.compose.animation.*
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusDirection
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.*
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//// ─── Welcome sheet ─────────────────────────────────────────────────────────────
//
//@Composable
//fun WelcomeSheet(
//    accent: Color,
//    onLoginClick: () -> Unit,
//    onSignUpClick: () -> Unit,
//    onGoogleClick: () -> Unit,
//    isLoading: Boolean
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .navigationBarsPadding()
//            .padding(horizontal = 24.dp)
//            .padding(top = 8.dp, bottom = 32.dp),
//        verticalArrangement = Arrangement.spacedBy(14.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            "Get Started",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.ExtraBold,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        Text(
//            "Join thousands building their best selves.",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = TextAlign.Center
//        )
//
//        Spacer(Modifier.height(4.dp))
//
//        // Primary CTA
//        FitButton(
//            text = "Create Account",
//            onClick = onSignUpClick,
//            accent = accent,
//            isLoading = isLoading
//        )
//
//        // Secondary CTA — outlined
//        OutlinedButton(
//            onClick = onLoginClick,
//            modifier = Modifier.fillMaxWidth().height(56.dp),
//            shape = RoundedCornerShape(18.dp),
//            border = ButtonDefaults.outlinedButtonBorder.copy(
//                brush = Brush.linearGradient(listOf(accent.copy(0.7f), accent.copy(0.3f)))
//            )
//        ) {
//            Text("Log In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accent)
//        }
//
//        // Divider
//        OrDivider()
//
//        // Google
//        SocialButton(
//            label = "Continue with Google",
//            icon = Icons.Default.Language,
//            onClick = onGoogleClick
//        )
//
//        Text(
//            "By continuing you agree to our Terms & Privacy Policy",
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//// ─── Login sheet ───────────────────────────────────────────────────────────────
//
//@Composable
//fun LoginSheet(
//    accent: Color,
//    uiState: AuthUiState,
//    onLogin: (String, String) -> Unit,
//    onForgotPassword: () -> Unit,
//    onSwitchToSignUp: () -> Unit
//) {
//    val focusManager = LocalFocusManager.current
//    var email    by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var showPass by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .navigationBarsPadding()
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 24.dp)
//            .padding(top = 4.dp, bottom = 32.dp),
//        verticalArrangement = Arrangement.spacedBy(14.dp)
//    ) {
//        AuthTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = "Email",
//            leadingIcon = Icons.Default.Email,
//            keyboardType = KeyboardType.Email,
//            imeAction = ImeAction.Next,
//            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
//        )
//
//        AuthTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = "Password",
//            leadingIcon = Icons.Default.Lock,
//            keyboardType = KeyboardType.Password,
//            imeAction = ImeAction.Done,
//            isPassword = true,
//            showPassword = showPass,
//            onTogglePassword = { showPass = !showPass },
//            onImeAction = {
//                focusManager.clearFocus()
//                if (email.isNotBlank() && password.length >= 6)
//                    onLogin(email, password)
//            }
//        )
//
//        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
//            TextButton(onClick = onForgotPassword) {
//                Text("Forgot password?", color = accent, fontWeight = FontWeight.SemiBold)
//            }
//        }
//
//        FitButton(
//            text = "Log In",
//            onClick = { onLogin(email, password) },
//            accent = accent,
//            isLoading = uiState.isLoading,
//            enabled = email.isNotBlank() && password.length >= 6
//        )
//
//        SwitchModeRow(
//            prompt = "Don't have an account?",
//            action = "Sign Up",
//            accent = accent,
//            onClick = onSwitchToSignUp
//        )
//    }
//}
//
//// ─── Sign-up sheet ─────────────────────────────────────────────────────────────
//
//@Composable
//fun SignUpSheet(
//    accent: Color,
//    uiState: AuthUiState,
//    onSignUp: (String, String, String) -> Unit,
//    onSwitchToLogin: () -> Unit
//) {
//    val focusManager = LocalFocusManager.current
//    var name     by remember { mutableStateOf("") }
//    var email    by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirm  by remember { mutableStateOf("") }
//    var showPass by remember { mutableStateOf(false) }
//
//    // Password strength 0–3
//    val strength = when {
//        password.length >= 12 &&
//                password.any { it.isDigit() } &&
//                password.any { !it.isLetterOrDigit() } -> 3
//        password.length >= 8 && password.any { it.isDigit() } -> 2
//        password.length >= 6 -> 1
//        else -> 0
//    }
//    val strengthLabel = listOf("", "Weak", "Good", "Strong")[strength]
//    val strengthColor = listOf(
//        Color.Transparent, Color(0xFFE53935), Color(0xFFFFB300), Color(0xFF43A047)
//    )[strength]
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .navigationBarsPadding()
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 24.dp)
//            .padding(top = 4.dp, bottom = 32.dp),
//        verticalArrangement = Arrangement.spacedBy(14.dp)
//    ) {
//        AuthTextField(
//            value = name,
//            onValueChange = { name = it },
//            label = "Full Name",
//            leadingIcon = Icons.Default.Person,
//            imeAction = ImeAction.Next,
//            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
//        )
//        AuthTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = "Email",
//            leadingIcon = Icons.Default.Email,
//            keyboardType = KeyboardType.Email,
//            imeAction = ImeAction.Next,
//            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
//        )
//        AuthTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = "Password",
//            leadingIcon = Icons.Default.Lock,
//            keyboardType = KeyboardType.Password,
//            imeAction = ImeAction.Next,
//            isPassword = true,
//            showPassword = showPass,
//            onTogglePassword = { showPass = !showPass },
//            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
//        )
//
//        // Strength bar
//        AnimatedVisibility(visible = password.isNotEmpty()) {
//            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    (1..3).forEach { level ->
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(4.dp)
//                                .clip(RoundedCornerShape(50))
//                                .background(
//                                    if (level <= strength) strengthColor
//                                    else MaterialTheme.colorScheme.surfaceVariant
//                                )
//                        )
//                    }
//                }
//                Text(
//                    strengthLabel,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = strengthColor,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//        }
//
//        AuthTextField(
//            value = confirm,
//            onValueChange = { confirm = it },
//            label = "Confirm Password",
//            leadingIcon = Icons.Default.LockOpen,
//            keyboardType = KeyboardType.Password,
//            imeAction = ImeAction.Done,
//            isPassword = true,
//            showPassword = showPass,
//            onTogglePassword = { showPass = !showPass },
//            onImeAction = { focusManager.clearFocus() },
//            isError = confirm.isNotEmpty() && confirm != password,
//            supportingText = if (confirm.isNotEmpty() && confirm != password)
//                "Passwords don't match" else null
//        )
//
//        FitButton(
//            text = "Create Account",
//            onClick = { onSignUp(name, email, password) },
//            accent = accent,
//            isLoading = uiState.isLoading,
//            enabled = name.isNotBlank() && email.isNotBlank()
//                    && password.length >= 6 && password == confirm
//        )
//
//        SwitchModeRow(
//            prompt = "Already have an account?",
//            action = "Log In",
//            accent = accent,
//            onClick = onSwitchToLogin
//        )
//    }
//}
//
//// ─── Forgot password sheet ─────────────────────────────────────────────────────
//
//@Composable
//fun ForgotPasswordSheet(
//    accent: Color,
//    uiState: AuthUiState,
//    onSendReset: (String) -> Unit,
//    onClearReset: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//
//    // Reset the "sent" flag when this sheet first composes
//    DisposableEffect(Unit) { onDispose { onClearReset() } }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .navigationBarsPadding()
//            .padding(horizontal = 24.dp)
//            .padding(top = 4.dp, bottom = 32.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        AnimatedContent(targetState = uiState.resetEmailSent, label = "ResetState") { sent ->
//            if (sent) {
//                // ── Success state ──────────────────────────────────────────────
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Spacer(Modifier.height(8.dp))
//                    Surface(
//                        modifier = Modifier.size(72.dp),
//                        shape = CircleShape,
//                        color = Color(0xFF43A047).copy(alpha = 0.15f)
//                    ) {
//                        Box(contentAlignment = Alignment.Center) {
//                            Icon(
//                                Icons.Default.MarkEmailRead,
//                                contentDescription = null,
//                                tint = Color(0xFF43A047),
//                                modifier = Modifier.size(36.dp)
//                            )
//                        }
//                    }
//                    Text(
//                        "Check your inbox",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                    Text(
//                        "We sent a reset link to\n$email",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            } else {
//                // ── Input state ────────────────────────────────────────────────
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(16.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        "Enter your account email and we'll send you a link to reset your password.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    AuthTextField(
//                        value = email,
//                        onValueChange = { email = it },
//                        label = "Email",
//                        leadingIcon = Icons.Default.Email,
//                        keyboardType = KeyboardType.Email,
//                        imeAction = ImeAction.Done,
//                        onImeAction = {
//                            if (email.isNotBlank()) onSendReset(email)
//                        }
//                    )
//                    FitButton(
//                        text = "Send Reset Link",
//                        onClick = { onSendReset(email) },
//                        accent = accent,
//                        isLoading = uiState.isLoading,
//                        enabled = email.isNotBlank()
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─── Shared reusable components ────────────────────────────────────────────────
//
//@Composable
//fun FitButton(
//    text: String,
//    onClick: () -> Unit,
//    accent: Color,
//    isLoading: Boolean = false,
//    enabled: Boolean = true,
//    modifier: Modifier = Modifier
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth().height(56.dp),
//        shape = RoundedCornerShape(18.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = accent),
//        enabled = enabled && !isLoading
//    ) {
//        AnimatedContent(targetState = isLoading, label = "BtnContent") { loading ->
//            if (loading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(22.dp),
//                    strokeWidth = 2.5.dp,
//                    color = Color.White
//                )
//            } else {
//                Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
//            }
//        }
//    }
//}
//
//@Composable
//fun AuthTextField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    label: String,
//    leadingIcon: ImageVector,
//    keyboardType: KeyboardType = KeyboardType.Text,
//    imeAction: ImeAction = ImeAction.Next,
//    isPassword: Boolean = false,
//    showPassword: Boolean = false,
//    onTogglePassword: (() -> Unit)? = null,
//    onImeAction: () -> Unit = {},
//    isError: Boolean = false,
//    supportingText: String? = null
//) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        label = { Text(label) },
//        leadingIcon = {
//            Icon(leadingIcon, contentDescription = null,
//                tint = MaterialTheme.colorScheme.onSurfaceVariant)
//        },
//        trailingIcon = if (isPassword) {
//            {
//                IconButton(onClick = { onTogglePassword?.invoke() }) {
//                    Icon(
//                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        } else null,
//        visualTransformation = if (isPassword && !showPassword)
//            PasswordVisualTransformation() else VisualTransformation.None,
//        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
//        keyboardActions = KeyboardActions(
//            onNext = { onImeAction() },
//            onDone = { onImeAction() }
//        ),
//        isError = isError,
//        supportingText = supportingText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier.fillMaxWidth(),
//        singleLine = true
//    )
//}
//
//@Composable
//private fun SocialButton(
//    label: String,
//    icon: ImageVector,
//    modifier: Modifier = Modifier,
//    onClick: () -> Unit
//) {
//    OutlinedButton(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth().height(52.dp),
//        shape = RoundedCornerShape(16.dp),
//        border = ButtonDefaults.outlinedButtonBorder.copy(
//            brush = Brush.linearGradient(
//                listOf(
//                    MaterialTheme.colorScheme.outline.copy(0.4f),
//                    MaterialTheme.colorScheme.outline.copy(0.2f)
//                )
//            )
//        )
//    ) {
//        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp),
//            tint = MaterialTheme.colorScheme.onSurface)
//        Spacer(Modifier.width(8.dp))
//        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
//    }
//}
//
//@Composable
//private fun OrDivider() {
//    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
//        HorizontalDivider(modifier = Modifier.weight(1f))
//        Text(
//            "  or  ",
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        HorizontalDivider(modifier = Modifier.weight(1f))
//    }
//}
//
//@Composable
//private fun SwitchModeRow(prompt: String, action: String, accent: Color, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(prompt, style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant)
//        TextButton(onClick = onClick) {
//            Text(action, color = accent, fontWeight = FontWeight.Bold)
//        }
//    }
//}