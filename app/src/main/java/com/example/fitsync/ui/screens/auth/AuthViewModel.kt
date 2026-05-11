package com.example.fitsync.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import com.example.fitsync.data.repository.AuthRepository
import com.example.fitsync.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean         = false,
    val errorMessage: String?      = null,
    val resetEmailSent: Boolean    = false,
    val isAuthenticated: Boolean   = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val prefs: PreferenceManager // 1. INJECT PREFERENCES
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state — if user is already signed in, skip auth screens
        viewModelScope.launch {
            repo.currentUserFlow.collect { user ->
                _uiState.update { it.copy(isAuthenticated = user != null) }
            }
        }
    }

    // ── Email login ────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repo.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    // 2. Initialize local prefs with user data from the server/repo
                    result.user?.displayName?.let { name ->
                        prefs.saveUserData(name, prefs.getUserGoal())
                    }
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AuthResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    // ── Email sign-up ──────────────────────────────────────────────────────────

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repo.createUserWithEmail(email, password, name)) {
                is AuthResult.Success -> {
                    // 3. Save the name locally immediately after registration
                    prefs.saveUserData(name, prefs.getUserGoal())
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AuthResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    // ── Password reset ─────────────────────────────────────────────────────────

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Assuming the repo returns Loading/Success for email trigger
            when (val result = repo.sendPasswordReset(email)) {
                is AuthResult.Loading -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true)
                }
                is AuthResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                // If your repo returns Success for reset, handle it here
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true)
                }
            }
        }
    }

    // ── Google ─────────────────────────────────────────────────────────────────

    fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repo.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    // 4. Save Google display name locally
                    result.user?.displayName?.let { name ->
                        prefs.saveUserData(name, prefs.getUserGoal())
                    }
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AuthResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    fun clearError()      { _uiState.update { it.copy(errorMessage = null) }    }
    fun clearResetState() { _uiState.update { it.copy(resetEmailSent = false) } }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            // Optionally clear local preferences on logout if you want a fresh start
            // prefs.clearAll()
        }
    }
}