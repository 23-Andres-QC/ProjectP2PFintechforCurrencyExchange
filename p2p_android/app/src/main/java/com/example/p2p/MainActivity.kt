package com.example.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.p2p.core.network.ApiClient
import com.example.p2p.navigation.NavGraph
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.P2PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            P2PTheme {
                Surface(color = BackgroundApp) {
                    NavGraph()
                }
            }
        }
    }
}
