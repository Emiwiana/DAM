package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.model.db.CharacterEquipmentEntity
import com.emiwiana.forge5e.model.db.CharacterSpellEntity
import com.emiwiana.forge5e.model.repository.CharacterRepository
import com.emiwiana.forge5e.model.repository.SrdRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AttackInfo(
    val name: String,
    val damage: String?,
    val damageType: String?,
    val range: String?,
    val source: String
)

data class FeatureInfo(
    val name: String,
    val description: String,
    val source: String // "Race" or "Class"
)

class CharacterDetailViewModel(
    private val characterId: Int,
    private val characterRepository: CharacterRepository,
    private val srdRepository: SrdRepository
) : ViewModel() {

    val character: StateFlow<CharacterEntity?> = characterRepository.getCharacterById(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<CharacterEquipmentEntity>> = characterRepository.getCharacterEquipment(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spells: StateFlow<List<CharacterSpellEntity>> = characterRepository.getCharacterSpells(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _attacks = MutableStateFlow<List<AttackInfo>>(emptyList())
    val attacks: StateFlow<List<AttackInfo>> = _attacks.asStateFlow()

    private val _features = MutableStateFlow<List<FeatureInfo>>(emptyList())
    val features: StateFlow<List<FeatureInfo>> = _features.asStateFlow()

    private val _availableEquipment = MutableStateFlow<List<APIReference>>(emptyList())
    val availableEquipment: StateFlow<List<APIReference>> = _availableEquipment.asStateFlow()

    private val _armorClass = MutableStateFlow(10)
    val armorClass: StateFlow<Int> = _armorClass.asStateFlow()

    init {
        combine(inventory, spells) { inv, spl ->
            updateAttacks(inv, spl)
        }.launchIn(viewModelScope)

        character.onEach { char ->
            char?.let {
                updateFeatures(it)
            }
        }.launchIn(viewModelScope)

        // Calculate AC whenever character (dex) or inventory (armor) changes
        combine(character, inventory) { char, inv ->
            char?.let { calculateAC(it, inv) }
        }.launchIn(viewModelScope)

        fetchAvailableEquipment()
    }

    private fun fetchAvailableEquipment() {
        viewModelScope.launch {
            srdRepository.fetchAllAvailableEquipment().onSuccess { list ->
                _availableEquipment.value = list.results
            }
        }
    }

    private suspend fun calculateAC(char: CharacterEntity, inv: List<CharacterEquipmentEntity>) {
        val dexMod = getModifier(char.dexterity)
        var baseAC = 10 + dexMod
        
        val equippedArmor = inv.filter { it.isEquipped }
        
        for (item in equippedArmor) {
            val detail = srdRepository.fetchEquipment(item.equipmentIndex).getOrNull()
            if (detail?.equipmentCategory?.index == "armor" && detail.armorClass != null) {
                val ac = detail.armorClass
                var currentArmorAC = ac.base
                if (ac.dexBonus) {
                    val bonus = if (ac.maxBonus != null) minOf(dexMod, ac.maxBonus) else dexMod
                    currentArmorAC += bonus
                }
                baseAC = currentArmorAC
                break // Only one armor affects AC as per user request
            }
        }
        
        // Add shields if equipped (Simplified: adding +2 if any shield is equipped)
        val hasShield = equippedArmor.any { it.name.lowercase().contains("shield") }
        if (hasShield) baseAC += 2

        _armorClass.value = baseAC
    }

    private suspend fun updateFeatures(char: CharacterEntity) {
        val newFeatures = mutableListOf<FeatureInfo>()
        
        // Race Features
        if (char.raceIndex.isNotBlank()) {
            srdRepository.fetchRaceTraits(char.raceIndex).onSuccess { traits ->
                traits.results.forEach { traitRef ->
                    srdRepository.fetchRacialFeature(traitRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Race"))
                    }
                }
            }
        }

        // Class Features
        if (char.classIndex.isNotBlank()) {
            srdRepository.fetchClassFeatures(char.classIndex).onSuccess { features ->
                features.results.forEach { featureRef ->
                    srdRepository.fetchClassFeature(featureRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Class"))
                    }
                }
            }
        }
        _features.value = newFeatures
    }

    private suspend fun updateAttacks(inv: List<CharacterEquipmentEntity>, spl: List<CharacterSpellEntity>) {
        val newAttacks = mutableListOf<AttackInfo>()
        inv.filter { it.isEquipped }.forEach { item ->
            srdRepository.fetchEquipment(item.equipmentIndex).onSuccess { equipment ->
                equipment.damage?.let { damage ->
                    newAttacks.add(
                        AttackInfo(
                            name = equipment.name,
                            damage = damage.damageDice,
                            damageType = damage.damageType.name,
                            range = equipment.weaponRange ?: "Melee",
                            source = "Equipment"
                        )
                    )
                }
            }
        }
        spl.forEach { spellRef ->
            srdRepository.fetchSpell(spellRef.spellIndex).onSuccess { spell ->
                if (spell.level > 0 || spell.range != "Self") {
                     newAttacks.add(
                        AttackInfo(
                            name = spell.name,
                            damage = null,
                            damageType = spell.school.name,
                            range = spell.range,
                            source = "Spell"
                        )
                    )
                }
            }
        }
        _attacks.value = newAttacks
    }

    fun levelUp() {
        viewModelScope.launch {
            character.value?.let { char ->
                if (char.level < 20) {
                    characterRepository.insert(char.copy(level = char.level + 1))
                }
            }
        }
    }

    fun updateExperience(xp: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(experience = xp))
            }
        }
    }

    fun updateDeathSaves(successes: Int, failures: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(
                    deathSaveSuccesses = successes.coerceIn(0, 3),
                    deathSaveFailures = failures.coerceIn(0, 3)
                ))
            }
        }
    }

    fun updateExhaustion(level: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(exhaustionLevel = level.coerceIn(0, 6)))
            }
        }
    }

    fun takeDamage(amount: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                var damageRemaining = amount
                var newTempHp = char.temporaryHp
                var newCurrentHp = char.currentHp

                if (newTempHp > 0) {
                    val tempReduction = minOf(newTempHp, damageRemaining)
                    newTempHp -= tempReduction
                    damageRemaining -= tempReduction
                }

                if (damageRemaining > 0) {
                    newCurrentHp = (newCurrentHp - damageRemaining).coerceAtLeast(0)
                }

                characterRepository.insert(char.copy(currentHp = newCurrentHp, temporaryHp = newTempHp))
            }
        }
    }

    fun heal(amount: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(currentHp = (char.currentHp + amount).coerceAtMost(char.maxHp)))
            }
        }
    }

    fun addTempHp(amount: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                if (amount > char.temporaryHp) {
                    characterRepository.insert(char.copy(temporaryHp = amount))
                }
            }
        }
    }

    fun addEquipmentFromSrd(index: String) {
        viewModelScope.launch {
            srdRepository.fetchEquipment(index).onSuccess { equipment ->
                characterRepository.addEquipment(
                    CharacterEquipmentEntity(
                        characterId = characterId,
                        equipmentIndex = equipment.index,
                        name = equipment.name,
                        weight = equipment.weight ?: 0.0,
                        quantity = 1,
                        isEquipped = false
                    )
                )
            }
        }
    }

    fun toggleEquip(item: CharacterEquipmentEntity) {
        viewModelScope.launch {
            val currentInventory = inventory.value
            
            // If equipping armor, unequip other armors first
            if (!item.isEquipped) {
                val detail = srdRepository.fetchEquipment(item.equipmentIndex).getOrNull()
                if (detail?.equipmentCategory?.index == "armor") {
                    currentInventory.forEach { otherItem ->
                        if (otherItem.isEquipped && otherItem.equipmentIndex != item.equipmentIndex) {
                            val otherDetail = srdRepository.fetchEquipment(otherItem.equipmentIndex).getOrNull()
                            if (otherDetail?.equipmentCategory?.index == "armor") {
                                characterRepository.updateEquipment(otherItem.copy(isEquipped = false))
                            }
                        }
                    }
                }
            }

            characterRepository.updateEquipment(item.copy(isEquipped = !item.isEquipped))
        }
    }
    
    fun removeEquipment(item: CharacterEquipmentEntity) {
        viewModelScope.launch {
            characterRepository.removeEquipment(item)
        }
    }

    fun removeSpell(spell: CharacterSpellEntity) {
        viewModelScope.launch {
            characterRepository.removeSpell(spell)
        }
    }

    // Calculation helpers
    fun getProficiencyBonus(level: Int) = 2 + (level - 1) / 4
    
    fun getModifier(score: Int) = (score - 10) / 2

    fun isSkillProficient(skillName: String): Boolean {
        return character.value?.skillProficiencies?.split(",")?.contains(skillName) ?: false
    }

    fun isSavingThrowProficient(ability: String): Boolean {
        return character.value?.savingThrowProficiencies?.split(",")?.contains(ability) ?: false
    }
}
