package com.karasu256.karasulab.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.data.ConnectionInfo
import com.karasu256.karasulab.data.ConnectionRepository
import com.karasu256.karasulab.data.DeveloperOptionsStore
import com.karasu256.karasulab.data.FirebaseProjectInfo
import com.karasu256.karasulab.data.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    connectionRepository: ConnectionRepository,
    private val developerOptionsStore: DeveloperOptionsStore,
    firebaseRepository: FirebaseRepository,
) : ViewModel() {
    val connection: StateFlow<ConnectionInfo?> = connectionRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val developerOptionsEnabled: StateFlow<Boolean> = developerOptionsStore.enabled

    val firebaseProjectInfo: FirebaseProjectInfo? = firebaseRepository.projectInfo()

    fun setDeveloperOptionsEnabled(enabled: Boolean) = developerOptionsStore.setEnabled(enabled)
}
