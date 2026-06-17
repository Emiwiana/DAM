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
    val source: String,
    val toHit: Int = 0,
    val damageBonus: Int = 0,
    val item: CharacterEquipmentEntity? = null,
    val spell: CharacterSpellEntity? = null,
    val properties: List<String> = emptyList()
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
        combine(character, inventory, spells) { char, inv, spl ->
            char?.let { updateAttacks(it, inv, spl) }
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

    private suspend fun updateAttacks(char: CharacterEntity, inv: List<CharacterEquipmentEntity>, spl: List<CharacterSpellEntity>) {
        val newAttacks = mutableListOf<AttackInfo>()
        val proficiencyBonus = getProficiencyBonus(char.level)
        val strMod = getModifier(char.strength)
        val dexMod = getModifier(char.dexterity)

        inv.filter { it.isEquipped }.forEach { item ->
            srdRepository.fetchEquipment(item.equipmentIndex).onSuccess { equipment ->
                equipment.damage?.let { damage ->
                    val properties = equipment.properties?.map { it.name } ?: emptyList()
                    val isFinesse = properties.contains("Finesse")
                    val isThrown = properties.contains("Thrown")
                    val isRanged = equipment.weaponRange == "Ranged"
                    val isVersatile = properties.contains("Versatile")

                    // Determine which modifier to use
                    val abilityMod = when {
                        item.selectedAbility == "STR" -> strMod
                        item.selectedAbility == "DEX" -> dexMod
                        isFinesse -> maxOf(strMod, dexMod)
                        isRanged && !isThrown -> dexMod
                        else -> strMod
                    }

                    // Check proficiency
                    val isProficient = isWeaponProficient(char, equipment.name, equipment.weaponCategory ?: "")
                    val toHit = abilityMod + (if (isProficient) proficiencyBonus else 0)
                    val damageBonus = abilityMod

                    var finalDamageDice = damage.damageDice
                    if (isVersatile && item.useTwoHanded) {
                        finalDamageDice = increaseDamageDie(finalDamageDice)
                    }

                    newAttacks.add(
                        AttackInfo(
                            name = equipment.name,
                            damage = finalDamageDice,
                            damageType = damage.damageType.name,
                            range = equipment.weaponRange + (if (equipment.categoryRange != null) " (${equipment.categoryRange})" else ""),
                            source = "Equipment",
                            toHit = toHit,
                            damageBonus = damageBonus,
                            item = item,
                            properties = properties
                        )
                    )
                }
            }
        }
        
        val defaultSpellMod = getModifierForAbility(char, char.spellcastingAbility)
        
        spl.forEach { spellRef ->
            srdRepository.fetchSpell(spellRef.spellIndex).onSuccess { spell ->
                // Check if spell is an attack or has damage
                // In D&D 5e SRD API, some spells have 'attack_type' or we check if it has damage
                // For simplicity, let's assume if it has a range that isn't 'Self', it might be an attack
                // Ideally we'd check for an 'attack' field in the spell detail if it existed.
                
                val spellAbilityMod = if (spellRef.customAbility != null) {
                    getModifierForAbility(char, spellRef.customAbility)
                } else {
                    defaultSpellMod
                }
                
                val toHit = spellAbilityMod + proficiencyBonus
                
                // Add spells with potential attacks to the attacks list
                // We'll filter for common combat cantrips/spells or anything that might need a roll
                if (spell.level == 0 || spell.range != "Self") {
                     newAttacks.add(
                        AttackInfo(
                            name = spell.name,
                            damage = null, // Detail doesn't always provide damage dice directly in top level
                            damageType = spell.school.name,
                            range = spell.range,
                            source = "Spell",
                            toHit = toHit,
                            spell = spellRef
                        )
                    )
                }
            }
        }
        _attacks.value = newAttacks
    }

    private fun increaseDamageDie(dice: String): String {
        return when {
            dice.contains("1d4") -> dice.replace("1d4", "1d6")
            dice.contains("1d6") -> dice.replace("1d6", "1d8")
            dice.contains("1d8") -> dice.replace("1d8", "1d10")
            dice.contains("1d10") -> dice.replace("1d10", "1d12")
            else -> dice
        }
    }

    private fun isWeaponProficient(char: CharacterEntity, weaponName: String, category: String): Boolean {
        val profs = char.weaponProficiencies.lowercase()
        return profs.contains(weaponName.lowercase()) || 
               profs.contains(category.lowercase()) || 
               (category.lowercase().contains("simple") && profs.contains("simple weapons")) ||
               (category.lowercase().contains("martial") && profs.contains("martial weapons"))
    }

    fun getModifierForAbility(char: CharacterEntity, ability: String): Int {
        return when(ability.uppercase()) {
            "STR" -> getModifier(char.strength)
            "DEX" -> getModifier(char.dexterity)
            "CON" -> getModifier(char.constitution)
            "INT" -> getModifier(char.intelligence)
            "WIS" -> getModifier(char.wisdom)
            "CHA" -> getModifier(char.charisma)
            else -> 0
        }
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

    fun updateMoney(cp: Int, sp: Int, ep: Int, gp: Int, pp: Int) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(cp = cp, sp = sp, ep = ep, gp = gp, pp = pp))
            }
        }
    }

    fun updateProficiencies(weapons: String, armor: String, tools: String, languages: String) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(
                    weaponProficiencies = weapons,
                    armorProficiencies = armor,
                    toolProficiencies = tools,
                    languages = languages
                ))
            }
        }
    }

    fun updateSpellcastingAbility(ability: String) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(char.copy(spellcastingAbility = ability))
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
    
    fun updateEquipmentAbility(item: CharacterEquipmentEntity, ability: String?) {
        viewModelScope.launch {
            characterRepository.updateEquipment(item.copy(selectedAbility = ability))
        }
    }

    fun updateEquipmentVersatile(item: CharacterEquipmentEntity, twoHanded: Boolean) {
        viewModelScope.launch {
            characterRepository.updateEquipment(item.copy(useTwoHanded = twoHanded))
        }
    }

    fun updateSpellAbility(spell: CharacterSpellEntity, ability: String?) {
        viewModelScope.launch {
            // Need to update repository/DAO to support this update. 
            // Assuming characterRepository has an updateSpell method.
            characterRepository.updateSpell(spell.copy(customAbility = ability))
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
