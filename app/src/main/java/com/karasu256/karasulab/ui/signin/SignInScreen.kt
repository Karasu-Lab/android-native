package com.karasu256.karasulab.ui.signin

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialAuthenticator
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.ui.UiText
import com.karasu256.karasulab.ui.component.AuthFrame
import com.karasu256.karasulab.ui.component.CodeField
import com.karasu256.karasulab.ui.component.EmailField
import com.karasu256.karasulab.ui.component.FormError
import com.karasu256.karasulab.ui.component.OrDivider
import com.karasu256.karasulab.ui.component.ProviderButtons
import com.karasu256.karasulab.ui.component.StepHost
import com.karasu256.karasulab.ui.providers.OAuthProvidersViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>(start = true)
@Composable
fun SignInScreen(
    navigator: DestinationsNavigator,
    addAccount: Boolean = false,
    switchToNewAccount: Boolean = false,
    viewModel: SignInViewModel = hiltViewModel(),
    providersViewModel: OAuthProvidersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val providersState by providersViewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val credentials = remember(activity) { CredentialAuthenticator(checkNotNull(activity)) }

    LaunchedEffect(providersViewModel) {
        providersViewModel.load("signIn")
    }

    LaunchedEffect(addAccount, switchToNewAccount) {
        viewModel.configure(
            addAccount,
            switchToNewAccount
        )
    }
    LaunchedEffect(viewModel) {
        viewModel.signedIn.collect {
            if (addAccount) {
                navigator.popBackStack()
            } else {
                navigator.navigate(SettingsScreenDestination) {
                    popUpTo(NavGraphs.root) {
                        inclusive = true
                    }
                }
            }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.openBrowser.collect { CustomTabsIntent.Builder().build().launchUrl(checkNotNull(activity), it.toUri()) }
    }
    BackHandler(enabled = state.step != SignInStep.Email) { viewModel.goBack() }
    BackHandler(enabled = state.busy) {}
    BackHandler(enabled = state.completingOAuth) {}

    Box(modifier = Modifier.fillMaxSize()) {
    AuthFrame(
        busy = state.busy,
        onBack = when {
            state.step != SignInStep.Email -> ({ viewModel.goBack() })
            addAccount -> ({ navigator.popBackStack() })
            else -> null
        },
    ) {
        StepHost(
            step = state.step,
            steps = SignInStep.entries,
            modifier = Modifier.fillMaxWidth()
        ) { step ->
            when (step) {
                SignInStep.Email -> EmailStep(
                    state = state,
                    providers = providersState.oauthProviders + listOfNotNull(providersState.passkeyProvider),
                    providersLoading = providersState.loading,
                    providersError = providersState.error,
                    onEmailChange = viewModel::onEmailChange,
                    onSubmit = viewModel::submitEmail,
                    onGoogle = { viewModel.signInWithGoogle(credentials) },
                    onPasskey = { viewModel.signInWithPasskey(credentials, fromStep = false) },
                    onOAuth = viewModel::signInWithOAuth,
                )

                SignInStep.Passkey -> PasskeyStep(
                    state = state,
                    onPasskey = { viewModel.signInWithPasskey(credentials, fromStep = true) },
                    onOtherMethods = viewModel::goToMethods,
                )

                SignInStep.Methods -> MethodsStep(state = state, onCode = viewModel::startCode)
                SignInStep.Code -> CodeStep(
                    state = state,
                    onCodeChange = viewModel::onCodeChange,
                    onSubmit = viewModel::submitCode,
                    onResend = viewModel::startCode,
                )
            }
        }
    }

    if (state.completingOAuth) {
        SigningInOverlay()
    }
    }
}

@Composable
private fun SigningInOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.signing_in),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun StepHeader(title: String, supporting: String? = null) {
    Column {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (supporting != null) {
            Text(
                text = supporting,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun EmailStep(
    state: SignInUiState,
    providers: List<OAuthProvider>,
    providersLoading: Boolean,
    providersError: UiText?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onPasskey: () -> Unit,
    onOAuth: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StepHeader(title = stringResource(if (state.addingAccount) R.string.sign_in_add_account_title else R.string.sign_in_title))
        EmailField(
            value = state.email,
            onValueChange = onEmailChange,
            enabled = !state.busy,
            error = state.emailError?.resolve(),
            imeAction = ImeAction.Done,
            onImeAction = onSubmit,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.busy && state.email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_next))
        }
        Spacer(Modifier.height(32.dp))
        OrDivider()
        Spacer(Modifier.height(24.dp))
        if (providersLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ProviderButtons(
                providers = providers,
                onGoogle = onGoogle,
                onPasskey = onPasskey,
                onOAuth = onOAuth,
                enabled = !state.busy,
                busyMethod = state.busyMethod,
                busyProviderId = state.busyProviderId,
            )
        }
        providersError?.let { FormError(text = it.resolve(), modifier = Modifier.padding(top = 12.dp)) }
    }
}

@Composable
private fun PasskeyStep(state: SignInUiState, onPasskey: () -> Unit, onOtherMethods: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StepHeader(title = stringResource(R.string.passkey_step_title), supporting = state.email)
        Button(onClick = onPasskey, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.passkey_use),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        state.passkeyError?.let {
            FormError(
                text = it.resolve(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        TextButton(
            onClick = onOtherMethods,
            enabled = !state.busy,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.methods_other))
        }
    }
}

@Composable
private fun MethodsStep(state: SignInUiState, onCode: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StepHeader(title = stringResource(R.string.methods_title), supporting = state.email)
        OutlinedButton(
            onClick = onCode,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Mail, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.code_link),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        state.methodsError?.let {
            FormError(
                text = it.resolve(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun CodeStep(
    state: SignInUiState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StepHeader(
            title = stringResource(R.string.code_title),
            supporting = stringResource(R.string.code_description, state.email)
        )
        CodeField(
            value = state.code,
            onValueChange = onCodeChange,
            label = stringResource(R.string.code_label),
            enabled = !state.busy,
            error = state.codeError?.resolve(),
            onImeAction = onSubmit,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.busy && state.code.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sign_in))
        }
        TextButton(
            onClick = onResend,
            enabled = !state.busy,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.code_resend))
        }
    }
}
