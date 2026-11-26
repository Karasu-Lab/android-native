package com.karasu256.karasulab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.karasu256.karasulab.data.auth.OAuthCallbackHub
import com.karasu256.karasulab.data.auth.NativeOAuthCallbackUri
import com.karasu256.karasulab.ui.loading.LoadingScreen
import com.karasu256.karasulab.ui.navigation.PushPopTransitions
import com.karasu256.karasulab.ui.splash.SplashScreen
import com.karasu256.karasulab.ui.theme.AppTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SignInScreenDestination
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var oauthCallbackHub: OAuthCallbackHub

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dispatchOAuthCallback(intent)
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LaunchFlow(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchOAuthCallback(intent)
    }

    private fun dispatchOAuthCallback(intent: Intent?) {
        val uri = intent?.data ?: return
        intent.data = null
        if (NativeOAuthCallbackUri.parse(uri.toString()) != null) {
            viewModel.onOAuthCallback(uri.toString())
        } else {
            oauthCallbackHub.deliver(uri)
        }
    }
}

private enum class LaunchPhase {
    Splash,
    Loading,
    Content,
}

@Composable
private fun LaunchFlow(viewModel: MainViewModel) {
    var phase by rememberSaveable { mutableStateOf(LaunchPhase.Splash) }

    Crossfade(targetState = phase, label = "launch") { current ->
        when (current) {
            LaunchPhase.Splash -> SplashScreen(onFinished = { phase = LaunchPhase.Loading })
            LaunchPhase.Loading -> LoadingScreen(onFinished = { phase = LaunchPhase.Content })
            LaunchPhase.Content -> AppContent(viewModel)
        }
    }
}

@Composable
private fun AppContent(viewModel: MainViewModel) {
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val signedInAtLaunch = isSignedIn ?: return
    val start = remember { if (signedInAtLaunch) SettingsScreenDestination else SignInScreenDestination() }
    val navController = rememberNavController()
    val navigator = navController.rememberDestinationsNavigator()

    LaunchedEffect(isSignedIn) {
        when {
            isSignedIn == false && navController.currentDestination?.route != SignInScreenDestination.route -> {
                navigator.navigate(SignInScreenDestination()) {
                    popUpTo(NavGraphs.root) { inclusive = true }
                }
            }
        }
    }

    DestinationsNavHost(
        navGraph = NavGraphs.root,
        start = start,
        navController = navController,
        defaultTransitions = PushPopTransitions,
    )
}
