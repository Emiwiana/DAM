package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.domain.*
import com.emiwiana.forge5e.model.repository.SrdRepository
import com.emiwiana.forge5e.model.repository.toDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Stack

class SrdBrowserViewModel(private val repository: SrdRepository) : ViewModel() {

    private val _categoryState = MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val categoryState: StateFlow<CategoryUiState> = _categoryState.asStateFlow()

    private val _cardState = MutableStateFlow<CardUiState>(CardUiState.Idle)
    val cardState: StateFlow<CardUiState> = _cardState.asStateFlow()

    private val _subItemsState = MutableStateFlow<SubItemsUiState>(SubItemsUiState.Idle)
    val subItemsState: StateFlow<SubItemsUiState> = _subItemsState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(BrowseCategory.CLASS)
    val selectedCategory: StateFlow<BrowseCategory> = _selectedCategory.asStateFlow()

    // History tracking
    private val history = Stack<Pair<String, BrowseCategory>>()
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private var currentItem: Pair<String, BrowseCategory>? = null

    init {
        selectCategory(_selectedCategory.value)
    }

    fun selectCategory(category: BrowseCategory) {
        _selectedCategory.value = category
        _cardState.value = CardUiState.Idle
        _subItemsState.value = SubItemsUiState.Idle
        
        history.clear()
        _canGoBack.value = false
        currentItem = null

        viewModelScope.launch {
            _categoryState.value = CategoryUiState.Loading
            val result = when (category) {
                BrowseCategory.SPELL -> repository.fetchAllAvailableSpells()
                BrowseCategory.CLASS -> repository.fetchAllAvailableClasses()
                BrowseCategory.RACE -> repository.fetchAllAvailableRaces()
                BrowseCategory.BACKGROUND -> repository.fetchAllAvailableBackgrounds()
                BrowseCategory.FEAT -> repository.fetchAllAvailableFeats()
                BrowseCategory.EQUIPMENT -> repository.fetchAllAvailableEquipment()
                else -> return@launch
            }

            result.onSuccess { _categoryState.value = CategoryUiState.Success(it.results) }
                .onFailure { 
                    it.printStackTrace()
                    _categoryState.value = CategoryUiState.Error("Failed to fetch ${category.name}: ${it.localizedMessage}") 
                }
        }
    }

    fun loadFeatureCard(index: String, category: BrowseCategory = _selectedCategory.value, isBackNavigation: Boolean = false) {
        if (!isBackNavigation) {
            currentItem?.let {
                history.push(it)
                _canGoBack.value = true
            }
        }
        currentItem = Pair(index, category)

        viewModelScope.launch {
            _cardState.value = CardUiState.Loading
            _subItemsState.value = SubItemsUiState.Loading

            when (category) {
                BrowseCategory.CLASS -> {
                    repository.fetchCharacterClass(index).onSuccess {
                        _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel())
                        loadClassSubItems(index)
                    }.onFailure { _cardState.value = CardUiState.Error("Failed to load class") }
                }
                BrowseCategory.RACE -> {
                    repository.fetchRace(index).onSuccess {
                        _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel())
                        loadRaceSubItems(index)
                    }.onFailure { _cardState.value = CardUiState.Error("Failed to load race") }
                }
                BrowseCategory.SUBCLASS -> repository.fetchSubclass(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.CLASS_FEATURE -> repository.fetchClassFeature(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.SUBRACE -> repository.fetchSubrace(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.RACIAL_FEATURE -> repository.fetchRacialFeature(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.SPELL -> repository.fetchSpell(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.FEAT -> repository.fetchFeat(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.BACKGROUND -> repository.fetchBackground(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                BrowseCategory.EQUIPMENT -> repository.fetchEquipment(index).onSuccess { _cardState.value = CardUiState.ShowBrowserItem(it.toDomainModel()) }
                else -> { _cardState.value = CardUiState.Error("Unsupported category") }
            }
        }
    }

    fun navigateBack() {
        if (history.isNotEmpty()) {
            val previous = history.pop()
            _canGoBack.value = history.isNotEmpty()
            loadFeatureCard(previous.first, previous.second, isBackNavigation = true)
        }
    }

    private suspend fun loadClassSubItems(classIndex: String) {
        val subclasses = repository.fetchClassSubclasses(classIndex).getOrNull()?.results ?: emptyList()
        val features = repository.fetchClassFeatures(classIndex).getOrNull()?.results ?: emptyList()
        _subItemsState.value = SubItemsUiState.Success(subclasses, features)
    }

    private suspend fun loadRaceSubItems(raceIndex: String) {
        val subraces = repository.fetchRaceSubraces(raceIndex).getOrNull()?.results ?: emptyList()
        val traits = repository.fetchRaceTraits(raceIndex).getOrNull()?.results ?: emptyList()
        _subItemsState.value = SubItemsUiState.Success(subraces, traits)
    }
}

sealed interface SubItemsUiState {
    object Idle : SubItemsUiState
    object Loading : SubItemsUiState
    data class Success(val variants: List<APIReference>, val features: List<APIReference>) : SubItemsUiState
    data class Error(val message: String) : SubItemsUiState
}

sealed interface CategoryUiState {
    object Idle : CategoryUiState
    object Loading : CategoryUiState
    data class Success(val list: List<APIReference>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

sealed interface CardUiState {
    object Idle : CardUiState
    object Loading : CardUiState
    data class ShowBrowserItem(val item: IBrowserItem) : CardUiState
    data class Error(val message: String) : CardUiState
}