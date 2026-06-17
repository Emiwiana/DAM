package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.APIReferenceList
import com.emiwiana.forge5e.model.api.dto.character.characterClass.EquipmentChoice
import com.emiwiana.forge5e.model.api.dto.mechanics.EquipmentQuantity
import com.emiwiana.forge5e.model.domain.*
import com.emiwiana.forge5e.model.repository.SrdRepository
import com.emiwiana.forge5e.model.repository.toDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Stack

class SrdBrowserViewModel(private val repository: SrdRepository) : ViewModel() {

    private val _categoryState = MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val categoryState = _categoryState.asStateFlow()

    private val _cardState = MutableStateFlow<CardUiState>(CardUiState.Idle)
    val cardState = _cardState.asStateFlow()

    private val _subItemsState = MutableStateFlow<SubItemsUiState>(SubItemsUiState.Idle)
    val subItemsState = _subItemsState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(BrowseCategory.CLASS)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val history = Stack<Pair<String, BrowseCategory>>()
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack = _canGoBack.asStateFlow()

    private var currentItem: Pair<String, BrowseCategory>? = null

    init {
        selectCategory(_selectedCategory.value)
    }

    fun selectCategory(category: BrowseCategory) {
        _selectedCategory.value = category
        resetDetails()
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
            _categoryState.value = result.fold(
                onSuccess = { CategoryUiState.Success(it.results) },
                onFailure = { CategoryUiState.Error("Failed to fetch ${category.name}") }
            )
        }
    }

    private fun resetDetails() {
        _cardState.value = CardUiState.Idle
        _subItemsState.value = SubItemsUiState.Idle
    }

    fun loadFeatureCard(index: String, category: BrowseCategory = _selectedCategory.value, isBackNavigation: Boolean = false) {
        if (!isBackNavigation) {
            currentItem?.let { history.push(it); _canGoBack.value = true }
        }
        currentItem = index to category

        viewModelScope.launch {
            _cardState.value = CardUiState.Loading
            _subItemsState.value = SubItemsUiState.Idle

            val result: Result<Unit> = when (category) {
                BrowseCategory.CLASS -> repository.fetchCharacterClass(index).onSuccess { handleSuccess(it.toDomainModel()) { loadClassSubItems(index) } }.map { }
                BrowseCategory.RACE -> repository.fetchRace(index).onSuccess { handleSuccess(it.toDomainModel()) { loadRaceSubItems(index) } }.map { }
                BrowseCategory.SUBCLASS -> repository.fetchSubclass(index).onSuccess { handleSuccess(it.toDomainModel()) { loadContextualFeatures(index, repository::fetchSubclassFeatures, it.features) } }.map { }
                BrowseCategory.SUBRACE -> repository.fetchSubrace(index).onSuccess { handleSuccess(it.toDomainModel()) { loadContextualFeatures(index, repository::fetchSubraceTraits, it.racialTraits) } }.map { }
                BrowseCategory.BACKGROUND -> repository.fetchBackground(index).onSuccess { handleSuccess(it.toDomainModel()) { updateSubItems(equipment = it.startingEquipment ?: emptyList()) } }.map { }
                BrowseCategory.EQUIPMENT -> repository.fetchEquipment(index).onSuccess { handleSuccess(it.toDomainModel()) { updateSubItems(equipment = it.contents ?: emptyList()) } }.map { }
                BrowseCategory.CLASS_FEATURE -> repository.fetchClassFeature(index).onSuccess { handleSuccess(it.toDomainModel()) }.map { }
                BrowseCategory.RACIAL_FEATURE -> repository.fetchRacialFeature(index).onSuccess { handleSuccess(it.toDomainModel()) }.map { }
                BrowseCategory.SPELL -> repository.fetchSpell(index).onSuccess { handleSuccess(it.toDomainModel()) }.map { }
                BrowseCategory.FEAT -> repository.fetchFeat(index).onSuccess { handleSuccess(it.toDomainModel()) }.map { }
                else -> Result.failure(Exception("Unsupported category"))
            }
            result.onFailure { _cardState.value = CardUiState.Error("Failed to load details") }
        }
    }

    private fun handleSuccess(item: IBrowserItem, subItemLoader: (suspend () -> Unit)? = null) {
        _cardState.value = CardUiState.ShowBrowserItem(item)
        subItemLoader?.let {
            _subItemsState.value = SubItemsUiState.Loading
            viewModelScope.launch { it() }
        }
    }

    private fun updateSubItems(
        variants: List<APIReference> = emptyList(),
        features: List<APIReference> = emptyList(),
        equipment: List<EquipmentQuantity> = emptyList(),
        equipmentChoices: List<EquipmentChoice> = emptyList()
    ) {
        _subItemsState.value = if (variants.isEmpty() && features.isEmpty() && equipment.isEmpty() && equipmentChoices.isEmpty()) {
            SubItemsUiState.Idle
        } else {
            SubItemsUiState.Success(variants, features, equipment, equipmentChoices)
        }
    }

    fun navigateBack() {
        if (history.isNotEmpty()) {
            val (index, category) = history.pop()
            _canGoBack.value = history.isNotEmpty()
            loadFeatureCard(index, category, isBackNavigation = true)
        }
    }

    private suspend fun loadClassSubItems(classIndex: String) {
        val subclasses = repository.fetchClassSubclasses(classIndex).getOrNull()?.results ?: emptyList()
        val features = repository.fetchClassFeatures(classIndex).getOrNull()?.results ?: emptyList()
        val startingEq = repository.fetchClassStartingEquipment(classIndex).getOrNull()
        updateSubItems(subclasses, features, startingEq?.startingEquipment ?: emptyList(), startingEq?.startingEquipmentOptions ?: emptyList())
    }

    private suspend fun loadRaceSubItems(raceIndex: String) {
        val subraces = repository.fetchRaceSubraces(raceIndex).getOrNull()?.results ?: emptyList()
        val traits = repository.fetchRaceTraits(raceIndex).getOrNull()?.results ?: emptyList()
        updateSubItems(variants = subraces, features = traits)
    }

    private suspend fun loadContextualFeatures(index: String, fetcher: suspend (String) -> Result<APIReferenceList>, fallback: List<APIReference>?) {
        val apiItems = fetcher(index).getOrNull()?.results ?: emptyList()
        updateSubItems(features = apiItems.ifEmpty { fallback ?: emptyList() })
    }
}

sealed interface SubItemsUiState {
    object Idle : SubItemsUiState
    object Loading : SubItemsUiState
    data class Success(
        val variants: List<APIReference> = emptyList(),
        val features: List<APIReference> = emptyList(),
        val equipment: List<EquipmentQuantity> = emptyList(),
        val equipmentChoices: List<EquipmentChoice> = emptyList()
    ) : SubItemsUiState
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
