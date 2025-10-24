package com.blackhat.multistep

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.blackhat.multistep.ui.components.HackerScreen

val FLAG = "DROIDGROUND_FLAG_PLACEHOLDER"
class StateMachineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateMachine(intent);
    }

    private fun getCurrentState(): Int {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return 0
        val currentState = sharedPref.getInt("currentState", 0)
        return currentState
    }

    private fun setCurrentState(state: Number) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("currentState", state.toInt())
            apply()
        }
    }

    private fun displayStuff(textToDisplay: String) {
        setContent {
            HackerScreen(
                message = textToDisplay,
                onScreenTap = { /* no-op */ }
            )
        }
    }

    fun stateMachine(intent: Intent) {
        var textToDisplay = "You just got here, right?"
        val action = intent.action
        val ordinal = getCurrentState()
        Log.d("BH", "Current state is ${ordinal}")
        Log.d("BH", "Received action is ${action}")
        if (ordinal != 0) {
            if (ordinal != 1) {
                if (ordinal == 2) {
                    setCurrentState(0)
                    Log.d("BH", "Final state, printing flag")
                    displayStuff(FLAG)
                    return
                }
            } else if ("GET_FLAG" == action) {
                setCurrentState(2)
                textToDisplay = "Transitioned from PREPARE to GET_FLAG."
                Log.d("BH", textToDisplay)
                displayStuff(textToDisplay)
                return
            }
        }  else if ("PREPARE_FLAG" == action) {
            setCurrentState(1)
            textToDisplay = "Transitioned from INIT to PREPARE."
            Log.d("BH", textToDisplay)
            displayStuff(textToDisplay)
            return
        }

        setCurrentState(0)
        displayStuff(textToDisplay)
    }
}