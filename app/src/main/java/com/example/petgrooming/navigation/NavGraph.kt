package com.example.petgrooming.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petgrooming.viewmodel.PetViewModel
import com.example.petgrooming.ui.screens.HomeScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    // Menggunakan PetViewModel yang mewarisi AndroidViewModel
    val viewModel: PetViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel)
        }
    }
}
