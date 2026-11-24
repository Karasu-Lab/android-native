package com.karasu256.karasulab.ui.signin

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialAuthenticator
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.ui.component.AuthFrame
import com.karasu256.karasulab.ui.component.CodeField
import com.karasu256.karasulab.ui.component.EmailChip
import com.karasu256.karasulab.ui.component.EmailField
import com.karasu256.karasulab.ui.component.FormError
import com.karasu256.karasulab.ui.component.OrDivider
import com.karasu256.karasulab.ui.component.ProviderButtons
import com.karasu256.karasulab.ui.component.StepHost
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
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val credentials = remember(activity) { activity?.let(::CredentialAuthenticator) }

    LaunchedEffect(addAccount, switchToNewAccount) { viewModel.configure(addAccount, switchToNewAccount) }
    LaunchedEffect(viewModel) {
        viewModel.signedIn.collect {
            if (addAccount) {
                navigator.popBackStack()
            } else {
                navigator.navigate(SettingsScreenDestination) { popUpTo(NavGraphs.root) { inclusive = true } }
            }
        }
    }
    BackHandler(enabled = state.step != SignInStep.Email) { viewModel.goBack() }

    AuthFrame(busy = state.busy) {
        StepHost(step = state.step, steps = SignInStep.entries, modifier = Modifier.fillMaxWidth()) { step ->
            when (step) {
                SignInStep.Email -> EmailStep(
                    state = state,
                    onEmailChange = viewModel::onEmailChange,
                    onSubmit = viewModel::submitEmail,
                    onGoogle = { credentials?.let(viewModel::signInWithGoogle) },
                    onPasskey = { credentials?.let { viewModel.signInWithPasskey(it, fromStep = false) } },
                )
                SignInStep.Passkey -> PasskeyStep(
                    state = state,
                    credentials = credentials,
                    onBack = viewModel::goBack,
                    onPasskey = { credentials?.let { viewModel.signInWithPasskey(it, fromStep = true) } },
                    onOtherMethods = viewModel::goToMethods,
                )
                SignInStep.Methods -> MethodsStep(
                    state = state,
                    onBack = viewModel::goBack,
                    onCode = viewModel::startCode,
                )
                SignInStep.Code -> CodeStep(
                    state = state,
                    onBack = viewModel::goBack,
                    onCodeChange = viewModel::onCodeChange,
                    onSubmit = viewModel::submitCode,
                    onResend = viewModel::startCode,
                )
            }
        }
    }
}

@Composable
private fun EmailStep(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onPasskey: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(if (state.addingAccount) R.string.sign_in_add_account_title else R.string.sign_in_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(24.dp))
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
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(stringResource(R.string.action_next))
        }
        Spacer(Modifier.height(24.dp))
        OrDivider()
        Spacer(Modifier.height(20.dp))
        ProviderButtons(onGoogle = onGoogle, onPasskey = onPasskey, enabled = !state.busy, busyMethod = state.busyMethod)
    }
}

@Composable
private fun PasskeyStep(
    state: SignInUiState,
    credentials: CredentialProvider?,
    onBack: () -> Unit,
    onPasskey: () -> Unit,
    onOtherMethods: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) { EmailChip(email = state.email, onClick = onBack, enabled = !state.busy) }
        Spacer(Modifier.height(16.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(text = stringResource(R.string.passkey_step_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPasskey, enabled = !state.busy && credentials != null, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.passkey_use))
        }
        state.passkeyError?.let { FormError(text = it.resolve(), modifier = Modifier.padding(top = 8.dp)) }
        TextButton(onClick = onOtherMethods, enabled = !state.busy, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.methods_other))
        }
    }
}

@Composable
private fun MethodsStep(state: SignInUiState, onBack: () -> Unit, onCode: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EmailChip(email = state.email, onClick = onBack, enabled = !state.busy)
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.methods_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onCode, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Mail, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(12.dp))
            Text(stringResource(R.string.code_link), modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        }
        state.methodsError?.let { FormError(text = it.resolve(), modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable
private fun CodeStep(
    state: SignInUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EmailChip(email = state.email, onClick = onBack, enabled = !state.busy)
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.code_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.code_description, state.email),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        CodeField(
            value = state.code,
            onValueChange = onCodeChange,
            label = stringResource(R.string.code_label),
            enabled = !state.busy,
            error = state.codeError?.resolve(),
            onImeAction = onSubmit,
        )
        TextButton(onClick = onResend, enabled = !state.busy, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.code_resend))
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.busy && state.code.length == 6,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(stringResource(R.string.sign_in))
        }
    }
}
