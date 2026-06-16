package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class DiceRollerViewModel : ViewModel() {

    private val _selectedDice = MutableStateFlow(20)
    val selectedDice: StateFlow<Int> = _selectedDice.asStateFlow()

    private val _numberOfDice = MutableStateFlow("1")
    val numberOfDice: StateFlow<String> = _numberOfDice.asStateFlow()

    private val _modifier = MutableStateFlow("0")
    val modifier: StateFlow<String> = _modifier.asStateFlow()

    private val _rollState = MutableStateFlow<RollUiState>(RollUiState.Idle)
    val rollState: StateFlow<RollUiState> = _rollState.asStateFlow()

    val diceOptions = listOf(4, 6, 8, 10, 12, 20, 100)

    fun onDiceTypeSelected(sides: Int) {
        _selectedDice.value = sides
    }

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

sealed interface RollUiState {
    object Idle : RollUiState
    data class Success(val rolls: List<Int>, val diceSum: Int, val modifier: Int, val total: Int) : RollUiState
}
