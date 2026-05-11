package com.example.fitsync.ui.screens.auth

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fitsync.R
import com.example.fitsync.ui.theme.LocalAccentColor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

// ── Gym images for the hero carousel ─────────────────────────────────────────
private val GymCarouselImages = listOf(
    "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&q=80",
    "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&q=80",
    "https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800&q=80",
    "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800&q=80",
    "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800&q=80",
)

private val CarouselCaptions = listOf(
    "Track every rep.",
    "Build your streak.",
    "Crush your goals.",
    "See your progress.",
    "Own your journey.",
)

// ─── MAIN SCREEN ──────────────────────────────────────────────────────────────

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val accent    = LocalAccentColor.current
    val uiState   by viewModel.uiState.collectAsState()
    val context   = LocalContext.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val modeStack = remember { mutableStateListOf(AuthSheetMode.Welcome) }
    val currentMode = modeStack.last()

    fun pushMode(mode: AuthSheetMode) { modeStack.add(mode) }
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun popMode()  { if (modeStack.size > 1) modeStack.removeLast() }

    val pagerState = rememberPagerState(pageCount = { GymCarouselImages.size })
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            val next = (pagerState.currentPage + 1) % GymCarouselImages.size
            pagerState.animateScrollToPage(next, animationSpec = tween(800))
        }
    }

    val inf = rememberInfiniteTransition(label = "orb")
    val orbScale by inf.animateFloat(
        0.9f, 1.1f,
        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.handleGoogleSignIn(it) }
        } catch (e: ApiException) {
            // handle silently or show snackbar
        }
    }
    fun launchGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = GymCarouselImages[page],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.52f))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 60.dp)
                    .scale(orbScale)
                    .blur(55.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.45f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = accent.copy(alpha = 0.20f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    "FitSync",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1.5).sp
                )

                AnimatedContent(
                    targetState = pagerState.currentPage,
                    transitionSpec = {
                        (fadeIn(tween(400)) + slideInVertically { it / 2 })
                            .togetherWith(fadeOut(tween(300)))
                    },
                    label = "Caption"
                ) { page ->
                    Text(
                        text = CarouselCaptions[page],
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(GymCarouselImages.size) { i ->
                        val isActive = pagerState.currentPage == i
                        val width by animateDpAsState(
                            if (isActive) 20.dp else 6.dp,
                            animationSpec = spring(Spring.DampingRatioMediumBouncy),
                            label = "dot"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) accent else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !showSheet,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .padding(horizontal = 24.dp),
                enter = fadeIn(tween(300)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(200)) + slideOutVertically { it / 2 }
            ) {
                FitButton(
                    text = "Get Started",
                    onClick = { showSheet = true },
                    accent = accent
                )
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),

                    dragHandle = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
                                    )
                            )
                            AnimatedVisibility(
                                visible = currentMode != AuthSheetMode.Welcome,
                                enter = fadeIn() + slideInVertically { -it },
                                exit = fadeOut() + slideOutVertically { -it }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { popMode() }) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = when (currentMode) {
                                            AuthSheetMode.Login          -> "Log In"
                                            AuthSheetMode.SignUp         -> "Create Account"
                                            AuthSheetMode.ForgotPassword -> "Reset Password"
                                            else                         -> ""
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                ) {
                    AuthSheetContent(
                        mode = currentMode,
                        accent = accent,
                        uiState = uiState,
                        onPush = ::pushMode,
                        onGoogleSignIn = ::launchGoogle,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ─── INNER SHEET ROUTER ───────────────────────────────────────────────────────

enum class AuthSheetMode { Welcome, Login, SignUp, ForgotPassword }

@Composable
private fun AuthSheetContent(
    mode: AuthSheetMode,
    accent: Color,
    uiState: AuthUiState,
    onPush: (AuthSheetMode) -> Unit,
    onGoogleSignIn: () -> Unit,
    viewModel: AuthViewModel
) {
    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally { it / 10 })
                .togetherWith(fadeOut(tween(150)))
        },
        label = "SheetMode"
    ) { m ->
        when (m) {
            AuthSheetMode.Welcome ->
                WelcomeSheet(
                    accent = accent,
                    onLoginClick  = { onPush(AuthSheetMode.Login)  },
                    onSignUpClick = { onPush(AuthSheetMode.SignUp) },
                    onGoogleClick = onGoogleSignIn,
                    isLoading = uiState.isLoading
                )
            AuthSheetMode.Login ->
                LoginSheet(
                    accent = accent,
                    uiState = uiState,
                    onLogin = viewModel::login,
                    onForgotPassword = { onPush(AuthSheetMode.ForgotPassword) },
                    onSwitchToSignUp = { onPush(AuthSheetMode.SignUp) }
                )
            AuthSheetMode.SignUp ->
                SignUpSheet(
                    accent = accent,
                    uiState = uiState,
                    onSignUp = viewModel::signUp,
                    onSwitchToLogin = { onPush(AuthSheetMode.Login) }
                )
            AuthSheetMode.ForgotPassword ->
                ForgotPasswordSheet(
                    accent = accent,
                    uiState = uiState,
                    onSendReset = viewModel::sendPasswordReset,
                    onClearReset = viewModel::clearResetState
                )
        }
    }
}

// ─── CHILD SHEETS ─────────────────────────────────────────────────────────────

@Composable
fun WelcomeSheet(
    accent: Color,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Get Started",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Join thousands building their best selves.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        FitButton(
            text = "Create Account",
            onClick = onSignUpClick,
            accent = accent,
            isLoading = isLoading
        )

        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(accent.copy(0.7f), accent.copy(0.3f)))
            )
        ) {
            Text("Log In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accent)
        }

        OrDivider()

        SocialButton(
            label = "Continue with Google",
            icon = Icons.Default.Language,
            onClick = onGoogleClick
        )

        Text(
            "By continuing you agree to our Terms & Privacy Policy",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginSheet(
    accent: Color,
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true,
            showPassword = showPass,
            onTogglePassword = { showPass = !showPass },
            onImeAction = {
                focusManager.clearFocus()
                if (email.isNotBlank() && password.length >= 6)
                    onLogin(email, password)
            }
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPassword) {
                Text("Forgot password?", color = accent, fontWeight = FontWeight.SemiBold)
            }
        }

        FitButton(
            text = "Log In",
            onClick = { onLogin(email, password) },
            accent = accent,
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.length >= 6
        )

        SwitchModeRow(
            prompt = "Don't have an account?",
            action = "Sign Up",
            accent = accent,
            onClick = onSwitchToSignUp
        )
    }
}

@Composable
fun SignUpSheet(
    accent: Color,
    uiState: AuthUiState,
    onSignUp: (String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val strength = when {
        password.length >= 12 &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8 && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
    val strengthLabel = listOf("", "Weak", "Good", "Strong")[strength]
    val strengthColor = listOf(
        Color.Transparent, Color(0xFFE53935), Color(0xFFFFB300), Color(0xFF43A047)
    )[strength]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isPassword = true,
            showPassword = showPass,
            onTogglePassword = { showPass = !showPass },
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        AnimatedVisibility(visible = password.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..3).forEach { level ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (level <= strength) strengthColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
                Text(
                    strengthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = strengthColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AuthTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirm Password",
            leadingIcon = Icons.Default.LockOpen,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true,
            showPassword = showPass,
            onTogglePassword = { showPass = !showPass },
            onImeAction = { focusManager.clearFocus() },
            isError = confirm.isNotEmpty() && confirm != password,
            supportingText = if (confirm.isNotEmpty() && confirm != password)
                "Passwords don't match" else null
        )

        FitButton(
            text = "Create Account",
            onClick = { onSignUp(name, email, password) },
            accent = accent,
            isLoading = uiState.isLoading,
            enabled = name.isNotBlank() && email.isNotBlank()
                    && password.length >= 6 && password == confirm
        )

        SwitchModeRow(
            prompt = "Already have an account?",
            action = "Log In",
            accent = accent,
            onClick = onSwitchToLogin
        )
    }
}

@Composable
fun ForgotPasswordSheet(
    accent: Color,
    uiState: AuthUiState,
    onSendReset: (String) -> Unit,
    onClearReset: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    DisposableEffect(Unit) { onDispose { onClearReset() } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(targetState = uiState.resetEmailSent, label = "ResetState") { sent ->
            if (sent) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color(0xFF43A047).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = Color(0xFF43A047),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        "Check your inbox",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "We sent a reset link to\n$email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Enter your account email and we'll send you a link to reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            if (email.isNotBlank()) onSendReset(email)
                        }
                    )
                    FitButton(
                        text = "Send Reset Link",
                        onClick = { onSendReset(email) },
                        accent = accent,
                        isLoading = uiState.isLoading,
                        enabled = email.isNotBlank()
                    )
                }
            }
        }
    }
}

// ─── SHARED COMPONENTS ────────────────────────────────────────────────────────

@Composable
fun FitButton(
    text: String,
    onClick: () -> Unit,
    accent: Color,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent),
        enabled = enabled && !isLoading
    ) {
        AnimatedContent(targetState = isLoading, label = "BtnContent") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
            } else {
                Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !showPassword)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        isError = isError,
        supportingText = supportingText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun SocialButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outline.copy(0.4f),
                    MaterialTheme.colorScheme.outline.copy(0.2f)
                )
            )
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            "  or  ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SwitchModeRow(prompt: String, action: String, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prompt, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onClick) {
            Text(action, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}