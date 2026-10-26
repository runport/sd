package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLog

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        AppLog.i("MainActivity", "Permission request results: $results")
        viewModel.refreshStorage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.i("MainActivity", "onCreate started. Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")

        // Guard enableEdgeToEdge for legacy TouchWiz Android 5.1.1
        try {
            enableEdgeToEdge()
            AppLog.i("MainActivity", "enableEdgeToEdge completed")
        } catch (t: Throwable) {
            AppLog.w("MainActivity", "enableEdgeToEdge not fully supported on this platform, skipping", t)
        }

        try {
            checkAndRequestPermissions()
        } catch (t: Throwable) {
            AppLog.w("MainActivity", "Error in checkAndRequestPermissions", t)
        }

        try {
            setContent {
                MyApplicationTheme {
                    MainScreen(viewModel = viewModel)
                }
            }
            AppLog.i("MainActivity", "setContent initialized successfully")
        } catch (t: Throwable) {
            AppLog.e("MainActivity", "Fatal error during setContent", t)
            throw t
        }
    }

    override fun onResume() {
        super.onResume()
        AppLog.d("MainActivity", "onResume: refreshing storage counters")
        try {
            viewModel.refreshStorage()
        } catch (t: Throwable) {
            AppLog.w("MainActivity", "Error in onResume refreshStorage", t)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            AppLog.i("MainActivity", "Requesting runtime permissions: $permissionsToRequest")
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
