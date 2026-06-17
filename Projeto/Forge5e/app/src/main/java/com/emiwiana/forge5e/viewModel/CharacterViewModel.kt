package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.model.repository.CharacterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {

    val allCharacters: StateFlow<List<CharacterEntity>> = repository.allCharacters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCharacter(name: String, raceIndex: String = "", classIndex: String = "") {
        viewModelScope.launch {
            repository.insert(
                CharacterEntity(
                    name = name,
                    raceIndex = raceIndex,
                    race = raceIndex.replaceFirstChar { it.uppercase() },
                    classIndex = classIndex,
                    characterClass = classIndex.replaceFirstChar { it.uppercase() },
                    level = 1,
                    maxHp = 10,
                    currentHp = 10
                )
            )
        }
    }

    fun deleteCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            repository.delete(character)
        }
    }
}
