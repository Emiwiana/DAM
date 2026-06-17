package com.emiwiana.forge5e

sealed class Screen(val route: String) {
    data object MainMenu : Screen("main_menu")
    data object Browser : Screen("browser")
    data object DiceRoller : Screen("dice_roller")
    data object Characters : Screen("characters")
    data object CharacterDetail : Screen("character_detail/{characterId}") {
        fun createRoute(characterId: Int) = "character_detail/$characterId"
    }
}
