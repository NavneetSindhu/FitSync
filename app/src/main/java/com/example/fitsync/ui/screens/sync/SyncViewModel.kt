package com.example.fitsync.ui.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import com.example.fitsync.data.remote.ApiService
import com.example.fitsync.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val apiService: ApiService, // 1. Inject the ApiService
    private val prefs: PreferenceManager
) : ViewModel() {

    // This now stores the real JSONBin Hex ID (e.g., 6608af...)
    private val _binId = MutableStateFlow(prefs.getBinId())
    val binId = _binId.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    // 2. Stats logic: Tracks what needs to be uploaded
    val syncStats = repository.getAllWorkouts().map { list ->
        val synced = list.count { it.isSynced }
        val pending = list.size - synced
        SyncUiState(syncedCount = synced, pendingCount = pending)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncUiState()
    )

    /**
     * The Master Sync Function
     * Handles both initial creation and future updates.
     */
    fun performSync() {
        viewModelScope.launch {
            _isSyncing.value = true

            // A. Get all local data
            val allWorkouts = repository.getAllWorkoutsSync()
            val currentId = prefs.getBinId()

            if (currentId.isEmpty()) {
                // B. CASE 1: No Cloud ID exists. Create a new "Bin"
                val newGeneratedId = apiService.createJournal(allWorkouts)
                if (newGeneratedId != null) {
                    prefs.saveBinId(newGeneratedId) // Save the random Hex
                    _binId.value = newGeneratedId
                    repository.markAllAsSynced()    // Update local DB
                }
            } else {
                // C. CASE 2: Cloud ID exists. Simply update the existing journal
                val success = apiService.updateJournal(currentId, allWorkouts)
                if (success) {
                    repository.markAllAsSynced()
                }
            }

            _isSyncing.value = false
        }
    }
}

// Ensure you have this data class defined for the UI
