package com.karasu256.karasulab.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.Account
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.LinkedProviderRepository
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.data.OAuthProviderRepository
import com.karasu256.karasulab.data.ProfileRepository
import com.karasu256.karasulab.data.ProviderProfileCacheRepository
import com.karasu256.karasulab.data.remote.attempt
import java.io.ByteArrayOutputStream
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
    val provider: OAuthProvider,
    val accountName: String?,
    val iconUrl: String,
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
    private val oauthProviderRepository: OAuthProviderRepository,
    private val profileCache: ProviderProfileCacheRepository,
) : ViewModel() {
    val account: StateFlow<Account?> = accountRepository.activeAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var providers: List<OAuthProvider> = emptyList()

    init {
        linkedProviderRepository.startStream(viewModelScope)
        viewModelScope.launch { attempt { linkedProviderRepository.profiles() } }
        viewModelScope.launch {
            attempt { oauthProviderRepository.list("link") }.onSuccess { fetched ->
                providers = fetched.filter(OAuthProvider::link)
                publishProviderIcons()
            }
        }
        viewModelScope.launch {
            profileCache.profiles.collect { publishProviderIcons() }
        }
    }

    private fun publishProviderIcons() {
        val profiles = profileCache.profiles.value
        val options = providers.mapNotNull { provider ->
            val profile = profiles[provider.id] ?: return@mapNotNull null
            val iconUrl = profile.iconUrl ?: return@mapNotNull null
            ProviderIconOption(provider, profile.name, iconUrl)
        }
        _uiState.update { it.copy(providerIcons = options) }
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

    fun useProviderIcon(option: ProviderIconOption) {
        if (option.provider.id == "minecraft") {
            changeIcon { profileRepository.uploadIcon(minecraftFaceBytes(option.iconUrl), "image/png") }
        } else {
            changeIcon { profileRepository.setIcon(option.iconUrl) }
        }
    }

    private suspend fun minecraftFaceBytes(textureUrl: String): ByteArray {
        val request = ImageRequest.Builder(context).data(textureUrl).allowHardware(false).build()
        val result = SingletonImageLoader.get(context).execute(request)
        val skin = (result as SuccessResult).image.toBitmap()
        val face = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(face)
        val paint = Paint().apply { isFilterBitmap = false }
        canvas.drawBitmap(Bitmap.createBitmap(skin, 8, 8, 8, 8), null, Rect(0, 0, 128, 128), paint)
        if (skin.height >= 64) {
            canvas.drawBitmap(Bitmap.createBitmap(skin, 40, 8, 8, 8), null, Rect(0, 0, 128, 128), paint)
        }
        val stream = ByteArrayOutputStream()
        face.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

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
