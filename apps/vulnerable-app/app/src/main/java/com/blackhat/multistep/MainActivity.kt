package com.blackhat.multistep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.blackhat.multistep.ui.components.HackerScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HackerScreen(
                message = "There's nothing here!",
                onScreenTap = { /* no-op */ }
            )
        }
    }
}