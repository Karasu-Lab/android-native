package com.karasu256.karasulab.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.BusyBar
import com.karasu256.karasulab.ui.component.LoadingView
import com.karasu256.karasulab.ui.component.UserAvatar
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SignInScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    loading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    menuViewModel: AccountMenuViewModel = hiltViewModel(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val menu by menuViewModel.uiState.collectAsStateWithLifecycle()
    val accounts by menuViewModel.accounts.collectAsStateWithLifecycle()
    val active by menuViewModel.activeAccount.collectAsStateWithLifecycle()
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var addDialogOpen by rememberSaveable { mutableStateOf(false) }
    val message = menu.message?.resolve()

    LaunchedEffect(menuViewModel) {
        menuViewModel.switched.collect {
            sheetOpen = false
            navigator.navigate(SettingsScreenDestination) { popUpTo(NavGraphs.root) { inclusive = true } }
        }
    }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            menuViewModel.messageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { navigator.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    active?.let { account ->
                        IconButton(onClick = { sheetOpen = true }) {
                            UserAvatar(
                                id = account.id,
                                label = account.displayName,
                                imageUrl = account.image,
                                size = 32.dp,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                active?.takeIf { !it.emailVerified }?.let {
                    EmailVerificationBanner(onSend = menuViewModel::resendVerification)
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        content = content,
                    )
                }
            }
            BusyBar(active = loading, modifier = Modifier.align(Alignment.TopCenter))
            when (menu.busy) {
                AccountMenuBusy.SigningOut -> BusyOverlay(stringResource(R.string.settings_signing_out))
                AccountMenuBusy.Switching -> BusyOverlay(stringResource(R.string.account_switching))
                AccountMenuBusy.None -> Unit
            }
        }
    }

    if (sheetOpen) {
        AccountSheet(
            accounts = accounts,
            removingId = menu.removingId,
            onSwitch = menuViewModel::switchTo,
            onRemove = menuViewModel::remove,
            onAddAccount = {
                sheetOpen = false
                addDialogOpen = true
            },
            onSignOut = {
                sheetOpen = false
                menuViewModel.signOut()
            },
            onDismiss = { sheetOpen = false },
        )
    }
    if (addDialogOpen) {
        AddAccountDialog(
            onContinue = { switchToNew ->
                addDialogOpen = false
                navigator.navigate(SignInScreenDestination(addAccount = true, switchToNewAccount = switchToNew))
            },
            onDismiss = { addDialogOpen = false },
        )
    }
}

@Composable
private fun BusyOverlay(message: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)) {
        LoadingView(message = message)
    }
}

@Composable
private fun EmailVerificationBanner(onSend: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.email_unverified_banner),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            TextButton(onClick = onSend) { Text(stringResource(R.string.email_unverified_send)) }
        }
    }
}
