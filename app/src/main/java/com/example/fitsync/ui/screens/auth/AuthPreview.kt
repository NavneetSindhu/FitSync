//package com.example.fitsync.ui.screens.auth
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.FitnessCenter
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.blur
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import kotlinx.coroutines.delay
//
//// ── Stub accent for preview (replaces LocalAccentColor) ───────────────────────
//private val PreviewAccent = Color(0xFFE53935)
//
//// ── Fake UI state (no ViewModel) ──────────────────────────────────────────────
//private val FakeUiState = AuthUiState(
//    isLoading        = false,
//    errorMessage     = null,
//    resetEmailSent   = false,
//    isAuthenticated  = false
//)
//
//// ── Carousel assets ───────────────────────────────────────────────────────────
//private val GymImages = listOf(
//    "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&q=80",
//    "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&q=80",
//    "https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800&q=80",
//    "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800&q=80",
//    "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800&q=80",
//)
//private val GymCaptions = listOf(
//    "Track every rep.",
//    "Build your streak.",
//    "Crush your goals.",
//    "See your progress.",
//    "Own your journey.",
//)
//
//// ─────────────────────────────────────────────────────────────────────────────
//// PREVIEW ENTRY POINT
//// Run this in Android Studio with the interactive preview (▶ button)
//// so you can tap buttons and navigate between sheets.
//// ─────────────────────────────────────────────────────────────────────────────
//
//@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
//@Preview(showSystemUi = true, name = "Auth Screen")
//@Composable
//fun AuthScreenPreview() {
//    // Wrap in MaterialTheme so colours resolve
//    MaterialTheme(colorScheme = darkColorScheme()) {
//        AuthScreenUI(accent = PreviewAccent)
//    }
//}
//
//@Preview(showSystemUi = true, name = "Auth Screen – Light")
//@Composable
//fun AuthScreenPreviewLight() {
//    MaterialTheme(colorScheme = lightColorScheme()) {
//        AuthScreenUI(accent = PreviewAccent)
//    }
//}
//
//// ── Standalone UI (no Firebase, no Hilt) ──────────────────────────────────────
//
//@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
//@Composable
//fun AuthScreenUI(accent: Color = PreviewAccent) {
//
//    // ── Mode stack — same logic as real AuthScreen ────────────────────────────
//    val modeStack = remember { mutableStateListOf(AuthSheetMode.Welcome) }
//    val currentMode = modeStack.last()
//    fun pushMode(m: AuthSheetMode) { modeStack.add(m) }
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun popMode()  { if (modeStack.size > 1) modeStack.removeLast() }
//
//    // ── Carousel auto-scroll ──────────────────────────────────────────────────
//    val pagerState = rememberPagerState(pageCount = { GymImages.size })
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(3500)
//            val next = (pagerState.currentPage + 1) % GymImages.size
//            pagerState.animateScrollToPage(next, animationSpec = tween(800))
//        }
//    }
//
//    // ── Orb pulse ─────────────────────────────────────────────────────────────
//    val inf = rememberInfiniteTransition(label = "orb")
//    val orbScale by inf.animateFloat(
//        0.9f, 1.1f,
//        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
//        label = "scale"
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        // ── Full-screen carousel ──────────────────────────────────────────────
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier.fillMaxSize(),
//            userScrollEnabled = false
//        ) { page ->
//            Box(Modifier.fillMaxSize()) {
//                AsyncImage(
//                    model = GymImages[page],
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//                Box(
//                    Modifier
//                        .fillMaxSize()
//                        .background(Color.Black.copy(alpha = 0.52f))
//                )
//            }
//        }
//
//        // ── Accent orb ────────────────────────────────────────────────────────
//        Box(
//            modifier = Modifier
//                .size(280.dp)
//                .align(Alignment.TopCenter)
//                .offset(y = 60.dp)
//                .scale(orbScale)
//                .blur(55.dp)
//                .clip(CircleShape)
//                .background(Brush.radialGradient(listOf(accent.copy(0.45f), Color.Transparent)))
//        )
//
//        // ── Hero text + dots ──────────────────────────────────────────────────
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopCenter)
//                .padding(top = 72.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Surface(
//                modifier = Modifier.size(72.dp),
//                shape = RoundedCornerShape(20.dp),
//                color = accent.copy(alpha = 0.20f)
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Icon(
//                        Icons.Default.FitnessCenter,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(36.dp)
//                    )
//                }
//            }
//
//            Text(
//                "FitSync",
//                fontSize = 44.sp,
//                fontWeight = FontWeight.Black,
//                color = Color.White,
//                letterSpacing = (-1.5).sp
//            )
//
//            AnimatedContent(
//                targetState = pagerState.currentPage,
//                transitionSpec = {
//                    (fadeIn(tween(400)) + slideInVertically { it / 2 })
//                        .togetherWith(fadeOut(tween(300)))
//                },
//                label = "Caption"
//            ) { page ->
//                Text(
//                    GymCaptions[page],
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Color.White.copy(alpha = 0.85f),
//                    fontWeight = FontWeight.Medium,
//                    letterSpacing = 0.3.sp
//                )
//            }
//
//            // Animated dot indicators
//            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//                repeat(GymImages.size) { i ->
//                    val isActive = pagerState.currentPage == i
//                    val width by animateDpAsState(
//                        if (isActive) 20.dp else 6.dp,
//                        spring(Spring.DampingRatioMediumBouncy),
//                        label = "dot"
//                    )
//                    Box(
//                        modifier = Modifier
//                            .height(6.dp)
//                            .width(width)
//                            .clip(CircleShape)
//                            .background(if (isActive) accent else Color.White.copy(0.4f))
//                    )
//                }
//            }
//        }
//
//        // ── Bottom sheet ──────────────────────────────────────────────────────
//        ModalBottomSheet(
//            onDismissRequest = {},
//            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
//            containerColor = MaterialTheme.colorScheme.surface,
//            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
//
//            dragHandle = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 12.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // Drag pill
//                    Box(
//                        modifier = Modifier
//                            .width(40.dp)
//                            .height(4.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f))
//                    )
//                    // Back row
//                    AnimatedVisibility(
//                        visible = currentMode != AuthSheetMode.Welcome,
//                        enter = fadeIn() + slideInVertically { -it },
//                        exit  = fadeOut() + slideOutVertically { -it }
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 8.dp, vertical = 4.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            IconButton(onClick = { popMode() }) {
//                                Icon(
//                                    Icons.AutoMirrored.Filled.ArrowBack,
//                                    contentDescription = "Back",
//                                    tint = MaterialTheme.colorScheme.onSurface
//                                )
//                            }
//                            Text(
//                                text = when (currentMode) {
//                                    AuthSheetMode.Login          -> "Log In"
//                                    AuthSheetMode.SignUp         -> "Create Account"
//                                    AuthSheetMode.ForgotPassword -> "Reset Password"
//                                    else                         -> ""
//                                },
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.onSurface
//                            )
//                        }
//                    }
//                }
//            }
//        ) {
//            // Route to the right sheet — all using stub uiState (no ViewModel)
//            AnimatedContent(
//                targetState = currentMode,
//                transitionSpec = {
//                    (fadeIn(tween(220)) + slideInHorizontally { it / 10 })
//                        .togetherWith(fadeOut(tween(150)))
//                },
//                label = "SheetMode"
//            ) { mode ->
//                when (mode) {
//                    AuthSheetMode.Welcome ->
//                        WelcomeSheet(
//                            accent        = accent,
//                            onLoginClick  = { pushMode(AuthSheetMode.Login) },
//                            onSignUpClick = { pushMode(AuthSheetMode.SignUp) },
//                            onGoogleClick = { /* no-op in preview */ },
//                            isLoading     = false
//                        )
//                    AuthSheetMode.Login ->
//                        LoginSheet(
//                            accent           = accent,
//                            uiState          = FakeUiState,
//                            onLogin          = { _, _ -> },
//                            onForgotPassword = { pushMode(AuthSheetMode.ForgotPassword) },
//                            onSwitchToSignUp = { pushMode(AuthSheetMode.SignUp) }
//                        )
//                    AuthSheetMode.SignUp ->
//                        SignUpSheet(
//                            accent          = accent,
//                            uiState         = FakeUiState,
//                            onSignUp        = { _, _, _ -> },
//                            onSwitchToLogin = { pushMode(AuthSheetMode.Login) }
//                        )
//                    AuthSheetMode.ForgotPassword ->
//                        ForgotPasswordSheet(
//                            accent       = accent,
//                            uiState      = FakeUiState,
//                            onSendReset  = { },
//                            onClearReset = { }
//                        )
//                }
//            }
//        }
//    }
//}