package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.character.characterClass.CharacterClass
import com.emiwiana.forge5e.model.api.dto.character.characterClass.ClassStartingEquipment
import com.emiwiana.forge5e.model.api.dto.character.race.Race
import com.emiwiana.forge5e.model.api.dto.character.race.Subrace
import com.emiwiana.forge5e.model.api.dto.mechanics.Background
import com.emiwiana.forge5e.model.api.dto.mechanics.Feat
import com.emiwiana.forge5e.model.api.dto.mechanics.ProficiencyCategory
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.model.db.CharacterEquipmentEntity
import com.emiwiana.forge5e.model.repository.CharacterRepository
import com.emiwiana.forge5e.model.repository.SrdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CharacterBuilderState(
    val step: Int = 1,
    val maxSteps: Int = 4,
    val name: String = "",
    val useMilestones: Boolean = false,
    val useEncumbrance: Boolean = true,
    val hpCalculationMode: String = "Average",
    val useFeats: Boolean = false,
    val hasStartingFeat: Boolean = false,
    
    // Step 2: Ability Scores (Base)
    val baseStr: Int = 10,
    val baseDex: Int = 10,
    val baseCon: Int = 10,
    val baseInt: Int = 10,
    val baseWis: Int = 10,
    val baseCha: Int = 10,

    // Step 3: Race & Background
    val selectedRace: Race? = null,
    val selectedSubrace: Subrace? = null,
    val selectedBackground: Background? = null,
    val availableRaces: List<APIReference> = emptyList(),
    val availableSubraces: List<APIReference> = emptyList(),
    val availableBackgrounds: List<APIReference> = emptyList(),

    // Step 4: Class & Subclass
    val selectedClass: CharacterClass? = null,
    val selectedSubclass: APIReference? = null,
    val availableClasses: List<APIReference> = emptyList(),
    val availableSubclasses: List<APIReference> = emptyList(),
    val startingEquipment: ClassStartingEquipment? = null,
    val equipmentSelections: Map<Int, String> = emptyMap(), // Choice Index -> Equipment Index
    val skillSelections: Map<Int, List<String>> = emptyMap(), // Choice Index -> Selected Prof Indices
    val resolvedProficiencyOptions: Map<Int, List<APIReference>> = emptyMap(), // Choice Index -> List of available refs

    // Step 5: Feat
    val selectedFeat: Feat? = null,
    val availableFeats: List<APIReference> = emptyList()
)

class CharacterBuilderViewModel(
    private val characterRepository: CharacterRepository,
    private val srdRepository: SrdRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterBuilderState())
    val uiState: StateFlow<CharacterBuilderState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            srdRepository.fetchAllAvailableRaces().onSuccess { races ->
                _uiState.update { it.copy(availableRaces = races.results) }
            }
            srdRepository.fetchAllAvailableBackgrounds().onSuccess { backgrounds ->
                _uiState.update { it.copy(availableBackgrounds = backgrounds.results) }
            }
            srdRepository.fetchAllAvailableClasses().onSuccess { classes ->
                _uiState.update { it.copy(availableClasses = classes.results) }
            }
            srdRepository.fetchAllAvailableFeats().onSuccess { feats ->
                _uiState.update { it.copy(availableFeats = feats.results) }
            }
        }
    }

    fun updateName(name: String) { _uiState.update { it.copy(name = name) } }
    fun updateUseMilestones(use: Boolean) { _uiState.update { it.copy(useMilestones = use) } }
    fun updateUseEncumbrance(use: Boolean) { _uiState.update { it.copy(useEncumbrance = use) } }
    fun updateHpCalculationMode(mode: String) { _uiState.update { it.copy(hpCalculationMode = mode) } }

    fun updateUseFeats(useFeats: Boolean) {
        _uiState.update { state ->
            val hasStarting = if (!useFeats) false else state.hasStartingFeat
            state.copy(
                useFeats = useFeats,
                hasStartingFeat = hasStarting,
                maxSteps = if (useFeats && hasStarting) 5 else 4
            ) 
        }
    }

    fun updateHasStartingFeat(has: Boolean) {
        _uiState.update { state ->
            state.copy(hasStartingFeat = has, maxSteps = if (state.useFeats && has) 5 else 4)
        }
    }

    fun updateAbilityScore(ability: String, score: Int) {
        _uiState.update { state ->
            when (ability.uppercase()) {
                "STR" -> state.copy(baseStr = score)
                "DEX" -> state.copy(baseDex = score)
                "CON" -> state.copy(baseCon = score)
                "INT" -> state.copy(baseInt = score)
                "WIS" -> state.copy(baseWis = score)
                "CHA" -> state.copy(baseCha = score)
                else -> state
            }
        }
    }

    fun selectRace(raceIndex: String) {
        viewModelScope.launch {
            srdRepository.fetchRace(raceIndex).onSuccess { race ->
                _uiState.update { it.copy(selectedRace = race, selectedSubrace = null) }
                srdRepository.fetchRaceSubraces(raceIndex).onSuccess { subraces ->
                    _uiState.update { it.copy(availableSubraces = subraces.results) }
                }
            }
        }
    }

    fun selectSubrace(subraceIndex: String) {
        viewModelScope.launch {
            srdRepository.fetchSubrace(subraceIndex).onSuccess { subrace ->
                _uiState.update { it.copy(selectedSubrace = subrace) }
            }
        }
    }

    fun selectBackground(backgroundIndex: String) {
        viewModelScope.launch {
            srdRepository.fetchBackground(backgroundIndex).onSuccess { background ->
                _uiState.update { it.copy(selectedBackground = background) }
            }
        }
    }

    fun selectClass(classIndex: String) {
        viewModelScope.launch {
            srdRepository.fetchCharacterClass(classIndex).onSuccess { clazz ->
                _uiState.update { it.copy(
                    selectedClass = clazz, 
                    selectedSubclass = null, 
                    equipmentSelections = emptyMap(), 
                    skillSelections = emptyMap(),
                    resolvedProficiencyOptions = emptyMap()
                ) }
                
                resolveProficiencyChoices(clazz)
                
                srdRepository.fetchClassSubclasses(classIndex).onSuccess { subclasses ->
                    _uiState.update { it.copy(availableSubclasses = subclasses.results) }
                }
                srdRepository.fetchClassStartingEquipment(classIndex).onSuccess { equip ->
                    _uiState.update { it.copy(startingEquipment = equip) }
                }
            }
        }
    }

    private suspend fun resolveProficiencyChoices(clazz: CharacterClass) {
        val resolved = mutableMapOf<Int, List<APIReference>>()
        clazz.proficiencyChoices?.forEachIndexed { index, choice ->
            val options = mutableListOf<APIReference>()
            choice.from.options?.forEach { opt ->
                if (opt.item != null) {
                    options.add(opt.item)
                } else if (opt.choice != null) {
                    if (opt.choice.from.proficiencyCategory != null) {
                        srdRepository.getProficiencyCategoryDetails(opt.choice.from.proficiencyCategory.index).onSuccess { cat: ProficiencyCategory ->
                            options.addAll(cat.proficiencies)
                        }
                    }
                }
            }
            if (choice.from.proficiencyCategory != null) {
                srdRepository.getProficiencyCategoryDetails(choice.from.proficiencyCategory.index).onSuccess { cat: ProficiencyCategory ->
                    options.addAll(cat.proficiencies)
                }
            }
            resolved[index] = options.distinctBy { it.index }
        }
        _uiState.update { it.copy(resolvedProficiencyOptions = resolved) }
    }

    fun selectSubclass(subclassRef: APIReference) {
        _uiState.update { it.copy(selectedSubclass = subclassRef) }
    }

    fun selectEquipment(choiceIndex: Int, equipmentIndex: String) {
        _uiState.update { state ->
            val newSelections = state.equipmentSelections.toMutableMap()
            newSelections[choiceIndex] = equipmentIndex
            state.copy(equipmentSelections = newSelections)
        }
    }

    fun selectSkill(choiceIndex: Int, skillIndex: String) {
        _uiState.update { state ->
            val currentSelections = state.skillSelections[choiceIndex] ?: emptyList()
            val maxSelections = state.selectedClass?.proficiencyChoices?.getOrNull(choiceIndex)?.choose ?: 1
            
            val newSelections = if (currentSelections.contains(skillIndex)) {
                currentSelections - skillIndex
            } else if (currentSelections.size < maxSelections) {
                currentSelections + skillIndex
            } else {
                currentSelections
            }
            
            val newMap = state.skillSelections.toMutableMap()
            newMap[choiceIndex] = newSelections
            state.copy(skillSelections = newMap)
        }
    }

    fun selectFeat(featIndex: String) {
        viewModelScope.launch {
            srdRepository.fetchFeat(featIndex).onSuccess { feat ->
                _uiState.update { it.copy(selectedFeat = feat) }
            }
        }
    }

    fun nextStep() { if (_uiState.value.step < _uiState.value.maxSteps) _uiState.update { it.copy(step = it.step + 1) } }
    fun previousStep() { _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) } }

    private fun isWeapon(prof: APIReference): Boolean {
        val idx = prof.index.lowercase()
        val name = prof.name.lowercase()
        val url = prof.url.lowercase()
        return url.contains("weapon") || idx.contains("weapon") || name.contains("weapon") ||
                idx.contains("simple") || idx.contains("martial") ||
                listOf("dagger", "dart", "sling", "quarterstaff", "crossbow", "longsword", "shortsword", "rapier", "scimitar", "greataxe", "greatsword", "halberd", "longbow", "shortbow", "mace", "morningstar", "pike", "trident", "warhammer", "war-pick", "flail", "glaive", "javelin", "lance", "sickle", "spear", "club")
                    .any { idx.contains(it) }
    }

    private fun isArmor(prof: APIReference): Boolean {
        val idx = prof.index.lowercase()
        val name = prof.name.lowercase()
        val url = prof.url.lowercase()
        return url.contains("armor") || idx.contains("armor") || name.contains("armor") ||
                idx.contains("shield") || name.contains("shield")
    }

    fun finishCreation() {
        val state = _uiState.value
        viewModelScope.launch {
            var fStr = state.baseStr; var fDex = state.baseDex; var fCon = state.baseCon
            var fInt = state.baseInt; var fWis = state.baseWis; var fCha = state.baseCha

            fun applyBonuses(bonuses: List<com.emiwiana.forge5e.model.api.dto.character.race.AbilityBonus>?) {
                bonuses?.forEach { b ->
                    when (b.abilityScore.index.lowercase()) {
                        "str" -> fStr += b.bonus; "dex" -> fDex += b.bonus; "con" -> fCon += b.bonus
                        "int" -> fInt += b.bonus; "wis" -> fWis += b.bonus; "cha" -> fCha += b.bonus
                    }
                }
            }
            applyBonuses(state.selectedRace?.abilityBonuses)
            applyBonuses(state.selectedSubrace?.abilityBonuses)

            val baseHp = (state.selectedClass?.hitDie ?: 10) + (fStr.let { 0 } /* Fix: use con mod */) // Actually con mod
            val conMod = (fCon - 10) / 2
            val finalBaseHp = (state.selectedClass?.hitDie ?: 10) + conMod
            
            val skillProfs = mutableSetOf<String>()
            val toolProfs = mutableSetOf<String>()
            val armorProfsList = mutableSetOf<String>()
            val weaponProfsList = mutableSetOf<String>()
            
            fun categorize(r: APIReference) {
                val name = r.name
                if (r.url.contains("skills") || r.index.startsWith("skill-")) {
                    skillProfs.add(name.removePrefix("Skill: "))
                } else if (isArmor(r)) {
                    armorProfsList.add(name)
                } else if (isWeapon(r)) {
                    weaponProfsList.add(name)
                } else {
                    toolProfs.add(name)
                }
            }

            // Background proficiencies
            state.selectedBackground?.startingProficiencies?.forEach { categorize(it) }
            
            // Class selected proficiencies
            state.skillSelections.forEach { (choiceIdx, selections) ->
                val available = state.resolvedProficiencyOptions[choiceIdx] ?: emptyList()
                selections.forEach { selIndex ->
                    available.find { it.index == selIndex }?.let { categorize(it) }
                }
            }
            
            // Class base proficiencies
            state.selectedClass?.proficiencies?.forEach { categorize(it) }
            
            val languages = state.selectedRace?.languages?.joinToString(",") { it.name } ?: ""

            val character = CharacterEntity(
                name = state.name.ifBlank { "New Hero" },
                characterClass = state.selectedClass?.name ?: "",
                classIndex = state.selectedClass?.index ?: "",
                subclass = state.selectedSubclass?.name ?: "",
                subclassIndex = state.selectedSubclass?.index ?: "",
                race = state.selectedRace?.name ?: "",
                raceIndex = state.selectedRace?.index ?: "",
                subrace = state.selectedSubrace?.name ?: "",
                subraceIndex = state.selectedSubrace?.index ?: "",
                background = state.selectedBackground?.name ?: "",
                backgroundIndex = state.selectedBackground?.index ?: "",
                level = 1,
                strength = fStr, dexterity = fDex, constitution = fCon,
                intelligence = fInt, wisdom = fWis, charisma = fCha,
                maxHp = finalBaseHp, currentHp = finalBaseHp,
                hitDieType = state.selectedClass?.hitDie ?: 8,
                currentHitDice = 1,
                useMilestones = state.useMilestones, useEncumbrance = state.useEncumbrance,
                hpCalculationMode = state.hpCalculationMode, useFeats = state.useFeats,
                hasStartingFeat = state.hasStartingFeat,
                featIndex = state.selectedFeat?.index ?: "",
                spellcastingAbility = state.selectedClass?.spellcasting?.spellcastingAbility?.name ?: "INT",
                savingThrowProficiencies = state.selectedClass?.savingThrows?.joinToString(",") { it.name } ?: "",
                skillProficiencies = skillProfs.joinToString(","),
                weaponProficiencies = weaponProfsList.joinToString(","),
                armorProficiencies = armorProfsList.joinToString(","),
                toolProficiencies = toolProfs.joinToString(","),
                languages = languages,
                baseSpeed = state.selectedRace?.speed ?: 30
            )

            val charId = characterRepository.insertAndGetId(character).toInt()

            // Equipment logic
            state.selectedBackground?.startingEquipment?.forEach { eqQty ->
                eqQty.equipment?.let { eq ->
                    characterRepository.addEquipment(CharacterEquipmentEntity(charId, eq.index, eq.name, eqQty.quantity, false, 0.0))
                }
            }
            state.startingEquipment?.startingEquipment?.forEach { eqQty ->
                eqQty.equipment?.let { eq ->
                    characterRepository.addEquipment(CharacterEquipmentEntity(charId, eq.index, eq.name, eqQty.quantity, false, 0.0))
                }
            }
            state.equipmentSelections.values.forEach { index ->
                srdRepository.fetchEquipment(index).onSuccess { d ->
                    characterRepository.addEquipment(CharacterEquipmentEntity(charId, d.index, d.name, 1, false, d.weight ?: 0.0))
                }
            }
        }
    }
}
