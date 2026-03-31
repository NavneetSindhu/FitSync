package com.example.fitsync.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.fitsync.data.local.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _showWelcomeDialog = MutableStateFlow(prefs.isFirstRun())
    val showWelcomeDialog = _showWelcomeDialog.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getUserName())
    val userName = _userName.asStateFlow()

    fun completeOnboarding(name: String, goal: String) {
        prefs.saveUserData(name, goal)
        prefs.setFirstRunCompleted()
        _userName.value = name
        _showWelcomeDialog.value = false
    }
}