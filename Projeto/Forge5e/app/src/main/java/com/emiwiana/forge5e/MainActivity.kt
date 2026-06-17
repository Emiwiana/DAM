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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emiwiana.forge5e.model.api.NetworkClient
import com.emiwiana.forge5e.model.db.AppDatabase
import com.emiwiana.forge5e.model.repository.CharacterRepository
import com.emiwiana.forge5e.model.repository.SrdRepository
import com.emiwiana.forge5e.ui.screens.CharacterBuilderScreen
import com.emiwiana.forge5e.ui.screens.CharacterDetailScreen
import com.emiwiana.forge5e.ui.screens.CharacterListScreen
import com.emiwiana.forge5e.ui.screens.DiceRollerScreen
import com.emiwiana.forge5e.ui.screens.MainMenuScreen
import com.emiwiana.forge5e.ui.screens.SrdBrowserScreen
import com.emiwiana.forge5e.ui.theme.Forge5eTheme
import com.emiwiana.forge5e.viewModel.CharacterBuilderViewModel
import com.emiwiana.forge5e.viewModel.CharacterDetailViewModel
import com.emiwiana.forge5e.viewModel.CharacterViewModel
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

                    val srdRepository = remember {
                        SrdRepository(NetworkClient.srdApiService)
                    }

                    val database = remember { AppDatabase.getDatabase(applicationContext) }
                    val characterRepository = remember { CharacterRepository(database.characterDao()) }

                    NavHost(navController = navController, startDestination = Screen.MainMenu.route) {

                        composable(Screen.MainMenu.route) {
                            MainMenuScreen(
                                onNavigateToBrowser = { navController.navigate(Screen.Browser.route) },
                                onNavigateToDiceRoller = { navController.navigate(Screen.DiceRoller.route) },
                                onNavigateToCharacters = { navController.navigate(Screen.Characters.route) }
                            )
                        }

                        composable(Screen.Characters.route) {
                            val viewModel: CharacterViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return CharacterViewModel(characterRepository) as T
                                    }
                                }
                            )
                            CharacterListScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { id ->
                                    navController.navigate(Screen.CharacterDetail.createRoute(id))
                                },
                                onNavigateToBuilder = {
                                    navController.navigate("builder")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.CharacterDetail.route,
                            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
                            val viewModel: CharacterDetailViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return CharacterDetailViewModel(
                                            characterId,
                                            characterRepository,
                                            srdRepository
                                        ) as T
                                    }
                                }
                            )
                            CharacterDetailScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("builder") {
                            val builderViewModel: CharacterBuilderViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return CharacterBuilderViewModel(
                                            characterRepository,
                                            srdRepository
                                        ) as T
                                    }
                                }
                            )
                            CharacterBuilderScreen(
                                viewModel = builderViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onFinish = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Browser.route) {
                            val viewModel: SrdBrowserViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return SrdBrowserViewModel(srdRepository) as T
                                    }
                                }
                            )

                            SrdBrowserScreen(viewModel = viewModel)
                        }

                        composable(Screen.DiceRoller.route) {
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
