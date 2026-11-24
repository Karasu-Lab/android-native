package com.karasu256.karasulab.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.Account
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.LinkedProviderRepository
import com.karasu256.karasulab.data.ProfileRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProviderIconOption(
    val providerId: String,
    val label: String,
    val avatarUrl: String,
)

data class ProfileUiState(
    val nameDialogOpen: Boolean = false,
    val savingName: Boolean = false,
    val nameError: UiText? = null,
    val iconSheetOpen: Boolean = false,
    val iconBusy: Boolean = false,
    val providerIcons: List<ProviderIconOption> = emptyList(),
    val message: UiText? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
    private val linkedProviderRepository: LinkedProviderRepository,
) : ViewModel() {
    val account: StateFlow<Account?> = accountRepository.activeAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            attempt { linkedProviderRepository.profiles() }.onSuccess { profiles ->
                val options = profiles.mapNotNull { (providerId, profile) ->
                    val avatar = profile?.avatarUrl ?: return@mapNotNull null
                    val provider = providerId.replaceFirstChar { it.uppercase() }
                    ProviderIconOption(providerId, profile.name?.let { "$it ($provider)" } ?: provider, avatar)
                }
                _uiState.update { it.copy(providerIcons = options) }
            }
        }
    }

    fun openNameDialog() = _uiState.update { it.copy(nameDialogOpen = true, nameError = null) }

    fun closeNameDialog() = _uiState.update { if (it.savingName) it else it.copy(nameDialogOpen = false) }

    fun openIconSheet() = _uiState.update { it.copy(iconSheetOpen = true) }

    fun closeIconSheet() = _uiState.update { it.copy(iconSheetOpen = false) }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun saveName(name: String) {
        if (_uiState.value.savingName) return
        _uiState.update { it.copy(savingName = true, nameError = null) }
        viewModelScope.launch {
            attempt { profileRepository.updateName(name) }
                .onSuccess { _uiState.update { it.copy(savingName = false, nameDialogOpen = false, message = UiText(R.string.profile_saved)) } }
                .onFailure { _uiState.update { it.copy(savingName = false, nameError = UiText(R.string.error_unknown)) } }
        }
    }

    fun uploadIcon(uri: Uri) = changeIcon(failure = R.string.profile_upload_failed) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
            ?: error("Unreadable image")
        profileRepository.uploadIcon(bytes, mimeType)
    }

    fun useProviderIcon(option: ProviderIconOption) = changeIcon { profileRepository.setIcon(option.avatarUrl) }

    fun deleteIcon() = changeIcon { profileRepository.setIcon(null) }

    private fun changeIcon(failure: Int = R.string.error_unknown, block: suspend () -> Unit) {
        if (_uiState.value.iconBusy) return
        _uiState.update { it.copy(iconBusy = true, iconSheetOpen = false) }
        viewModelScope.launch {
            val message = attempt(block).fold(
                onSuccess = { UiText(R.string.profile_icon_changed) },
                onFailure = { UiText(failure) },
            )
            _uiState.update { it.copy(iconBusy = false, message = message) }
        }
    }
}
