package com.emiwiana.forge5e.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.db.CharacterEntity
import com.emiwiana.forge5e.model.db.CharacterEquipmentEntity
import com.emiwiana.forge5e.model.db.CharacterSpellEntity
import com.emiwiana.forge5e.model.db.CharacterTrackerEntity
import com.emiwiana.forge5e.model.repository.CharacterRepository
import com.emiwiana.forge5e.model.repository.SrdRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    val source: String // "Race", "Class", "Background"
)

class CharacterDetailViewModel(
    private val characterId: Int,
    private val characterRepository: CharacterRepository,
    private val srdRepository: SrdRepository
) : ViewModel() {

    // --- State Flows ---

    val character: StateFlow<CharacterEntity?> = characterRepository.getCharacterById(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<CharacterEquipmentEntity>> = characterRepository.getCharacterEquipment(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spells: StateFlow<List<CharacterSpellEntity>> = characterRepository.getCharacterSpells(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackers: StateFlow<List<CharacterTrackerEntity>> = characterRepository.getCharacterTrackers(characterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _attacks = MutableStateFlow<List<AttackInfo>>(emptyList())
    val attacks: StateFlow<List<AttackInfo>> = _attacks.asStateFlow()

    private val _features = MutableStateFlow<List<FeatureInfo>>(emptyList())
    val features: StateFlow<List<FeatureInfo>> = _features.asStateFlow()

    private val _availableEquipment = MutableStateFlow<List<APIReference>>(emptyList())
    val availableEquipment: StateFlow<List<APIReference>> = _availableEquipment.asStateFlow()

    private val _armorClass = MutableStateFlow(10)
    val armorClass: StateFlow<Int> = _armorClass.asStateFlow()

    // --- Derived Stats ---

    val initiative: StateFlow<Int> = character.map { char ->
        char?.let { getModifier(it.dexterity) + it.initiativeBonus } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val speed: StateFlow<Int> = character.map { char ->
        char?.let { 30 + it.speedBonus } ?: 30 // Simplified base speed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val passivePerception: StateFlow<Int> = character.map { char ->
        char?.let {
            val profBonus = getProficiencyBonus(it.level)
            val isProficient = it.skillProficiencies.split(",").contains("Perception")
            val perceptionMod = getModifier(it.wisdom) + (if (isProficient) profBonus else 0)
            10 + perceptionMod
        } ?: 10
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    init {
        character.filterNotNull().onEach { char ->
            updateFeatures(char)
        }.launchIn(viewModelScope)

        combine(character, inventory, spells) { char, inv, spl ->
            if (char != null) {
                updateAttacks(char, inv, spl)
                calculateAC(char, inv)
            }
        }.launchIn(viewModelScope)

        fetchAvailableEquipment()
    }

    // --- Private Update Logic ---

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
            srdRepository.fetchEquipment(item.equipmentIndex).onSuccess { detail ->
                detail.armorClass?.let { ac ->
                    val bonus = if (ac.dexBonus) {
                        if (ac.maxBonus != null) minOf(dexMod, ac.maxBonus) else dexMod
                    } else 0
                    baseAC = ac.base + bonus
                }
            }
        }

        if (equippedArmor.any { it.name.lowercase().contains("shield") }) baseAC += 2
        _armorClass.value = baseAC
    }

    private suspend fun updateFeatures(char: CharacterEntity) {
        val newFeatures = mutableListOf<FeatureInfo>()

        // Fetch Race Traits
        if (char.raceIndex.isNotBlank()) {
            srdRepository.fetchRaceTraits(char.raceIndex).onSuccess { traits ->
                traits.results.forEach { traitRef ->
                    srdRepository.fetchRacialFeature(traitRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Race"))
                    }
                }
            }
        }

        // Fetch Subrace Traits
        if (char.subraceIndex.isNotBlank()) {
            srdRepository.fetchSubraceTraits(char.subraceIndex).onSuccess { traits ->
                traits.results.forEach { traitRef ->
                    srdRepository.fetchRacialFeature(traitRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Race"))
                    }
                }
            }
        }

        // Fetch Class Features
        if (char.classIndex.isNotBlank()) {
            srdRepository.fetchClassFeatures(char.classIndex).onSuccess { features ->
                features.results.forEach { featureRef ->
                    srdRepository.fetchClassFeature(featureRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Class"))
                    }
                }
            }
        }

        // Fetch Subclass Features
        if (char.subclassIndex.isNotBlank()) {
            srdRepository.fetchSubclassFeatures(char.subclassIndex).onSuccess { features ->
                features.results.forEach { featureRef ->
                    srdRepository.fetchClassFeature(featureRef.index).onSuccess { detail ->
                        newFeatures.add(FeatureInfo(detail.name, detail.desc.joinToString("\n"), "Class"))
                    }
                }
            }
        }

        // Fetch Background Feature
        if (char.backgroundIndex.isNotBlank()) {
            srdRepository.fetchBackground(char.backgroundIndex).onSuccess { background ->
                newFeatures.add(FeatureInfo(
                    background.backgroundFeature.name,
                    background.backgroundFeature.desc.joinToString("\n"),
                    "Background"
                ))
            }
        }

        _features.value = newFeatures
    }

    private suspend fun updateAttacks(char: CharacterEntity, inv: List<CharacterEquipmentEntity>, spl: List<CharacterSpellEntity>) {
        val newAttacks = mutableListOf<AttackInfo>()
        val profBonus = getProficiencyBonus(char.level)

        // Equipment Attacks
        inv.filter { it.isEquipped }.forEach { item ->
            srdRepository.fetchEquipment(item.equipmentIndex).onSuccess { eq ->
                eq.damage?.let { damage ->
                    val properties = eq.properties?.map { it.name } ?: emptyList()
                    val abilityMod = getWeaponAbilityModifier(char, item, eq, properties)
                    
                    val isProficient = isWeaponProficient(char, eq.name, eq.weaponCategory ?: "")
                    val toHit = abilityMod + (if (isProficient) profBonus else 0)
                    
                    var finalDamageDice = damage.damageDice
                    if (properties.contains("Versatile") && item.useTwoHanded) {
                        finalDamageDice = increaseDamageDie(finalDamageDice)
                    }

                    newAttacks.add(AttackInfo(
                        name = eq.name,
                        damage = finalDamageDice,
                        damageType = damage.damageType.name,
                        range = formatRange(eq),
                        source = "Equipment",
                        toHit = toHit,
                        damageBonus = abilityMod,
                        item = item,
                        properties = properties
                    ))
                }
            }
        }

        // Spell Attacks
        val spellAbilityMod = getModifierForAbility(char, char.spellcastingAbility)
        val attackSpells = if (char.preparesSpells) spl.filter { it.isPrepared } else spl
        
        attackSpells.forEach { spellRef ->
            srdRepository.fetchSpell(spellRef.spellIndex).onSuccess { spell ->
                if (spell.desc.any { it.contains("spell attack", ignoreCase = true) }) {
                    val mod = spellRef.customAbility?.let { getModifierForAbility(char, it) } ?: spellAbilityMod
                    newAttacks.add(AttackInfo(
                        name = spell.name,
                        damage = null,
                        damageType = spell.school.name,
                        range = spell.range,
                        source = "Spell",
                        toHit = mod + profBonus,
                        spell = spellRef
                    ))
                }
            }
        }
        _attacks.value = newAttacks
    }

    // --- Helper Methods ---

    fun getModifier(score: Int) = (score - 10) / 2
    fun getProficiencyBonus(level: Int) = 2 + (level - 1) / 4

    fun getModifierForAbility(char: CharacterEntity, ability: String): Int = when (ability.uppercase()) {
        "STR" -> getModifier(char.strength)
        "DEX" -> getModifier(char.dexterity)
        "CON" -> getModifier(char.constitution)
        "INT" -> getModifier(char.intelligence)
        "WIS" -> getModifier(char.wisdom)
        "CHA" -> getModifier(char.charisma)
        else -> 0
    }

    private fun getWeaponAbilityModifier(char: CharacterEntity, item: CharacterEquipmentEntity, eq: com.emiwiana.forge5e.model.api.dto.items.Equipment, props: List<String>): Int {
        if (item.selectedAbility != null) return getModifierForAbility(char, item.selectedAbility)
        
        val strMod = getModifier(char.strength)
        val dexMod = getModifier(char.dexterity)
        
        return when {
            props.contains("Finesse") -> maxOf(strMod, dexMod)
            eq.weaponRange == "Ranged" && !props.contains("Thrown") -> dexMod
            else -> strMod
        }
    }

    private fun formatRange(eq: com.emiwiana.forge5e.model.api.dto.items.Equipment): String {
        return eq.range?.let {
            if (it.long != null) "${it.normal}/${it.long}" else "${it.normal}"
        } ?: eq.weaponRange ?: "Melee"
    }

    private fun increaseDamageDie(dice: String): String = when {
        dice.contains("1d4") -> dice.replace("1d4", "1d6")
        dice.contains("1d6") -> dice.replace("1d6", "1d8")
        dice.contains("1d8") -> dice.replace("1d8", "1d10")
        dice.contains("1d10") -> dice.replace("1d10", "1d12")
        else -> dice
    }

    private fun isWeaponProficient(char: CharacterEntity, weaponName: String, category: String): Boolean {
        val profs = char.weaponProficiencies.lowercase()
        return profs.contains(weaponName.lowercase()) ||
               profs.contains(category.lowercase()) ||
               (category.lowercase().contains("simple") && profs.contains("simple weapons")) ||
               (category.lowercase().contains("martial") && profs.contains("martial weapons"))
    }

    fun isSkillProficient(skillName: String): Boolean {
        return character.value?.skillProficiencies?.lowercase()?.split(",")?.contains(skillName.lowercase()) ?: false
    }

    fun isSavingThrowProficient(ability: String): Boolean {
        return character.value?.savingThrowProficiencies?.uppercase()?.split(",")?.contains(ability.uppercase()) ?: false
    }

    // --- Public Actions ---

    private fun updateCharacter(action: (CharacterEntity) -> CharacterEntity) {
        viewModelScope.launch {
            character.value?.let { char ->
                characterRepository.insert(action(char))
            }
        }
    }

    fun levelUp() = updateCharacter { it.copy(level = (it.level + 1).coerceAtMost(20), currentHitDice = it.currentHitDice + 1) }
    fun updateExperience(xp: Int) = updateCharacter { it.copy(experience = xp) }
    fun updateDeathSaves(successes: Int, failures: Int) = updateCharacter { 
        it.copy(deathSaveSuccesses = successes.coerceIn(0, 3), deathSaveFailures = failures.coerceIn(0, 3)) 
    }
    fun updateExhaustion(level: Int) = updateCharacter { it.copy(exhaustionLevel = level.coerceIn(0, 6)) }

    fun takeDamage(amount: Int) = updateCharacter { char ->
        var damageRemaining = amount
        var tempHp = char.temporaryHp
        if (tempHp > 0) {
            val reduction = minOf(tempHp, damageRemaining)
            tempHp -= reduction
            damageRemaining -= reduction
        }
        char.copy(currentHp = (char.currentHp - damageRemaining).coerceAtLeast(0), temporaryHp = tempHp)
    }

    fun heal(amount: Int) = updateCharacter { it.copy(currentHp = (it.currentHp + amount).coerceAtMost(it.maxHp)) }
    fun addTempHp(amount: Int) = updateCharacter { if (amount > it.temporaryHp) it.copy(temporaryHp = amount) else it }
    fun updateHitDice(amount: Int) = updateCharacter { it.copy(currentHitDice = amount.coerceIn(0, it.level)) }

    fun spendHitDie() {
        character.value?.let { char ->
            if (char.currentHitDice > 0) {
                val healAmount = (Random.nextInt(1, 9) + getModifier(char.constitution)).coerceAtLeast(1)
                updateCharacter { it.copy(
                    currentHitDice = it.currentHitDice - 1,
                    currentHp = (it.currentHp + healAmount).coerceAtMost(it.maxHp)
                )}
            }
        }
    }

    fun shortRest() {
        viewModelScope.launch {
            trackers.value.filter { it.resetType == "Short Rest" }.forEach { 
                characterRepository.updateTracker(it.copy(current = it.max))
            }
            spells.value.filter { it.resetType == "Short Rest" }.forEach {
                characterRepository.updateSpell(it.copy(currentUses = it.maxUses))
            }
        }
    }

    fun longRest() {
        updateCharacter { char ->
            char.copy(
                currentHp = char.maxHp,
                currentHitDice = (char.currentHitDice + (char.level / 2).coerceAtLeast(1)).coerceAtMost(char.level),
                usedSlots1 = 0, usedSlots2 = 0, usedSlots3 = 0, usedSlots4 = 0, usedSlots5 = 0,
                usedSlots6 = 0, usedSlots7 = 0, usedSlots8 = 0, usedSlots9 = 0,
                deathSaveSuccesses = 0, deathSaveFailures = 0
            )
        }
        viewModelScope.launch {
            trackers.value.forEach { characterRepository.updateTracker(it.copy(current = it.max)) }
            spells.value.filter { it.maxUses > 0 }.forEach { characterRepository.updateSpell(it.copy(currentUses = it.maxUses)) }
        }
    }

    fun updateUsedSlots(level: Int, amount: Int) = updateCharacter { char ->
        when(level) {
            1 -> char.copy(usedSlots1 = amount)
            2 -> char.copy(usedSlots2 = amount)
            3 -> char.copy(usedSlots3 = amount)
            4 -> char.copy(usedSlots4 = amount)
            5 -> char.copy(usedSlots5 = amount)
            6 -> char.copy(usedSlots6 = amount)
            7 -> char.copy(usedSlots7 = amount)
            8 -> char.copy(usedSlots8 = amount)
            9 -> char.copy(usedSlots9 = amount)
            else -> char
        }
    }

    fun updateInitiativeBonus(bonus: Int) = updateCharacter { it.copy(initiativeBonus = bonus) }
    fun updateSpeedBonus(bonus: Int) = updateCharacter { it.copy(speedBonus = bonus) }
    fun updateSpellcastingAbility(ability: String) = updateCharacter { it.copy(spellcastingAbility = ability) }

    fun addEquipmentFromSrd(index: String) {
        viewModelScope.launch {
            srdRepository.fetchEquipment(index).onSuccess { eq ->
                characterRepository.addEquipment(CharacterEquipmentEntity(
                    characterId = characterId,
                    equipmentIndex = eq.index,
                    name = eq.name,
                    weight = eq.weight ?: 0.0,
                    quantity = 1,
                    isEquipped = false
                ))
            }
        }
    }

    fun toggleEquip(item: CharacterEquipmentEntity) {
        viewModelScope.launch {
            if (!item.isEquipped) {
                srdRepository.fetchEquipment(item.equipmentIndex).onSuccess { detail ->
                    if (detail.equipmentCategory?.index == "armor") {
                        inventory.value.filter { it.isEquipped && it.equipmentIndex != item.equipmentIndex }.forEach { other ->
                             srdRepository.fetchEquipment(other.equipmentIndex).onSuccess { otherDetail ->
                                 if (otherDetail.equipmentCategory?.index == "armor") {
                                     characterRepository.updateEquipment(other.copy(isEquipped = false))
                                 }
                             }
                        }
                    }
                }
            }
            characterRepository.updateEquipment(item.copy(isEquipped = !item.isEquipped))
        }
    }

    fun updateEquipmentAbility(item: CharacterEquipmentEntity, ability: String?) {
        viewModelScope.launch { characterRepository.updateEquipment(item.copy(selectedAbility = ability)) }
    }

    fun updateEquipmentVersatile(item: CharacterEquipmentEntity, twoHanded: Boolean) {
        viewModelScope.launch { characterRepository.updateEquipment(item.copy(useTwoHanded = twoHanded)) }
    }

    fun updateSpellAbility(spell: CharacterSpellEntity, ability: String?) {
        viewModelScope.launch { characterRepository.updateSpell(spell.copy(customAbility = ability)) }
    }

    fun updateSpellUses(spell: CharacterSpellEntity, current: Int, max: Int, reset: String) {
        viewModelScope.launch { characterRepository.updateSpell(spell.copy(currentUses = current, maxUses = max, resetType = reset)) }
    }

    fun useSpellUse(spell: CharacterSpellEntity) {
        viewModelScope.launch {
            if (spell.currentUses > 0) {
                characterRepository.updateSpell(spell.copy(currentUses = spell.currentUses - 1))
            }
        }
    }

    fun updateMoney(cp: Int, sp: Int, ep: Int, gp: Int, pp: Int) = updateCharacter { 
        it.copy(cp = cp, sp = sp, ep = ep, gp = gp, pp = pp) 
    }
    
    fun updateSpellcastingLimits(prepares: Boolean, maxPrepared: Int, maxKnown: Int) = updateCharacter {
        it.copy(preparesSpells = prepares, maxPreparedSpells = maxPrepared, maxKnownSpells = maxKnown)
    }

    fun removeEquipment(item: CharacterEquipmentEntity) = viewModelScope.launch { characterRepository.removeEquipment(item) }
    fun removeSpell(spell: CharacterSpellEntity) = viewModelScope.launch { characterRepository.removeSpell(spell) }
    fun toggleSpellPrepared(spell: CharacterSpellEntity) = viewModelScope.launch { characterRepository.updateSpell(spell.copy(isPrepared = !spell.isPrepared)) }

    fun addTracker(name: String, max: Int, resetType: String) = viewModelScope.launch {
        characterRepository.addTracker(CharacterTrackerEntity(characterId, name, max, max, resetType))
    }
    fun updateTracker(tracker: CharacterTrackerEntity, current: Int) = viewModelScope.launch {
        characterRepository.updateTracker(tracker.copy(current = current.coerceIn(0, tracker.max)))
    }
    fun removeTracker(tracker: CharacterTrackerEntity) = viewModelScope.launch { characterRepository.removeTracker(tracker) }

    suspend fun fetchSpellDetails(index: String) = srdRepository.fetchSpell(index)
    suspend fun fetchEquipmentDetails(index: String) = srdRepository.fetchEquipment(index)
}
