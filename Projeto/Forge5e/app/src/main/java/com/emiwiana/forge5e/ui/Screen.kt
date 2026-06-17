package com.emiwiana.forge5e.ui

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Characters : Screen("characters")
    object CharacterDetail : Screen("character_detail/{characterId}") {
        fun createRoute(characterId: Int) = "character_detail/$characterId"
    }
    object Builder : Screen("builder")
    object Browser : Screen("browser")
    object DiceRoller : Screen("dice_roller")
}
