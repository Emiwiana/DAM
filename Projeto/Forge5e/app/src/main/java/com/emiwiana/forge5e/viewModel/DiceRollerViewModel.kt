package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * ViewModel for the Dice Roller screen.
 * Handles the logic for selecting dice, entering quantities, and calculating rolls.
 */
class DiceRollerViewModel : ViewModel() {

    private val _selectedDice = MutableStateFlow(20)
    /**
     * The number of sides of the currently selected die.
     */
    val selectedDice: StateFlow<Int> = _selectedDice.asStateFlow()

    private val _numberOfDice = MutableStateFlow("1")
    /**
     * String representation of the number of dice to roll.
     */
    val numberOfDice: StateFlow<String> = _numberOfDice.asStateFlow()

    private val _modifier = MutableStateFlow("0")
    /**
     * String representation of the flat modifier to add to the roll result.
     */
    val modifier: StateFlow<String> = _modifier.asStateFlow()

    private val _rollState = MutableStateFlow<RollUiState>(RollUiState.Idle)
    /**
     * The current state of the roll result UI.
     */
    val rollState: StateFlow<RollUiState> = _rollState.asStateFlow()

    /**
     * List of standard dice options available to the user.
     */
    val diceOptions = listOf(4, 6, 8, 10, 12, 20, 100)

    /**
     * Updates the selected die type.
     * @param sides The number of sides for the selected die.
     */
    fun onDiceTypeSelected(sides: Int) {
        _selectedDice.value = sides
    }

    /**
     * Updates the number of dice to roll, with validation and capping at 1000.
     * @param count The input string for the number of dice.
     */
    fun onNumberOfDiceChanged(count: String) {
        if (count.isEmpty()) {
            _numberOfDice.value = ""
            return
        }
        if (count.all { it.isDigit() }) {
            // Cap at 1000. If length is > 4, it's already over 1000.
            val num = if (count.length > 4) 1001 else count.toIntOrNull() ?: 0
            if (num <= 1000) {
                _numberOfDice.value = count
            } else {
                _numberOfDice.value = "1000"
            }
        }
    }

    /**
     * Updates the roll modifier, allowing for negative numbers.
     * @param value The input string for the modifier.
     */
    fun onModifierChanged(value: String) {
        // Allow optional leading minus sign for negative modifiers
        if (value.isEmpty() || value == "-") {
            _modifier.value = value
            return
        }
        if (value.toIntOrNull() != null) {
            _modifier.value = value
        }
    }

    /**
     * Performs the dice roll logic using the current settings and updates [rollState].
     */
    fun rollDice() {
        val count = _numberOfDice.value.toIntOrNull() ?: 0
        val mod = _modifier.value.toIntOrNull() ?: 0
        if (count > 0) {
            val sides = _selectedDice.value
            val rolls = List(count) { Random.nextInt(1, sides + 1) }
            val sum = rolls.sum()
            _rollState.value = RollUiState.Success(rolls, sum, mod, sum + mod)
        }
    }
}

/**
 * Represents the UI state of the dice roller results.
 */
sealed interface RollUiState {
    /**
     * Initial state before any roll has been made.
     */
    object Idle : RollUiState

    /**
     * State representing a successful roll operation.
     *
     * @property rolls The list of individual die results.
     * @property diceSum The sum of all dice before the modifier.
     * @property modifier The flat modifier applied.
     * @property total The final result (sum + modifier).
     */
    data class Success(val rolls: List<Int>, val diceSum: Int, val modifier: Int, val total: Int) : RollUiState
}
