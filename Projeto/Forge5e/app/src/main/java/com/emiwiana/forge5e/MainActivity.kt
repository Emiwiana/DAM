package com.emiwiana.forge5e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emiwiana.forge5e.model.api.NetworkClient
import com.emiwiana.forge5e.model.repository.SrdRepository
import com.emiwiana.forge5e.ui.screens.DiceRollerScreen
import com.emiwiana.forge5e.ui.screens.MainMenuScreen
import com.emiwiana.forge5e.ui.screens.SrdBrowserScreen
import com.emiwiana.forge5e.ui.theme.Forge5eTheme
import com.emiwiana.forge5e.viewModel.DiceRollerViewModel
import com.emiwiana.forge5e.viewModel.SrdBrowserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Forge5eTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val repository = remember {
                        SrdRepository(NetworkClient.srdApiService)
                    }

                    NavHost(navController = navController, startDestination = "main_menu") {

                        composable("main_menu") {
                            MainMenuScreen(
                                onNavigateToBrowser = { navController.navigate("browser") },
                                onNavigateToDiceRoller = { navController.navigate("dice_roller") }
                            )
                        }

                        composable("browser") {
                            val viewModel: SrdBrowserViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return SrdBrowserViewModel(repository) as T
                                    }
                                }
                            )

                            SrdBrowserScreen(viewModel = viewModel)
                        }

                        composable("dice_roller") {
                            val diceViewModel: DiceRollerViewModel = viewModel()
                            DiceRollerScreen(
                                viewModel = diceViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}