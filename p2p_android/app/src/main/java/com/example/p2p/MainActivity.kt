package com.example.p2p

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.example.p2p.core.firebase.FcmTokenRegistrar
import com.example.p2p.core.network.ApiClient
import com.example.p2p.navigation.NavGraph
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.P2PTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        lifecycleScope.launch {
            FcmTokenRegistrar.registerIfLoggedIn(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        ensureNotificationChannel()
        requestNotificationPermissionIfNeeded()
        lifecycleScope.launch {
            FcmTokenRegistrar.registerIfLoggedIn(this@MainActivity)
        }
        enableEdgeToEdge()
        setContent {
            P2PTheme {
                Surface(color = BackgroundApp) {
                    NavGraph()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "p2p_channel",
            "P2P Exchange",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de transacciones y alertas"
        }
        manager.createNotificationChannel(channel)
    }
}
