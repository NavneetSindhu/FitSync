package com.example.fitsync.ui.screens.auth

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    // --- HEIGHT FIX: skipPartiallyExpanded = true forces the sheet to open fully! ---
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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