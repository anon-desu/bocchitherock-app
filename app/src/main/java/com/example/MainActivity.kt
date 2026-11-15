package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.BocchiNavHost
import com.example.ui.theme.BocchiTheme
import com.example.viewmodel.LoginViewModel
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BocchiApplication
        val mainFactory = MainViewModel.Factory(app.authRepository, app.musicPlayerController)
        val loginFactory = LoginViewModel.Factory(app.authRepository)

        setContent {
            BocchiTheme {
                val context = LocalContext.current
                if (Build.VERSION.SDK_INT >= 33) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                    LaunchedEffect(Unit) {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val mainViewModel: MainViewModel = viewModel(factory = mainFactory)
                BocchiNavHost(
                    mainViewModel = mainViewModel,
                    loginViewModelFactory = loginFactory
                )
            }
        }
    }
}
