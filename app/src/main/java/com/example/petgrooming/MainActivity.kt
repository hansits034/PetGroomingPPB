package com.example.petgrooming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.petgrooming.navigation.AppNavGraph
import com.example.petgrooming.ui.theme.PetgroomingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetgroomingTheme {
                AppNavGraph()
            }
        }
    }
}
