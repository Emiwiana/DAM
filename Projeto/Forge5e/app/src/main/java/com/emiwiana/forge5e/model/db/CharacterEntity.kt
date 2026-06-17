package com.emiwiana.forge5e.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val characterClass: String = "",
    val classIndex: String = "",
    val subclass: String = "",
    val subclassIndex: String = "",
    val race: String = "",
    val raceIndex: String = "",
    val subrace: String = "",
    val subraceIndex: String = "",
    val background: String = "",
    val backgroundIndex: String = "",
    val level: Int = 1,
    
    // Ability Scores
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    
    // HP
    val maxHp: Int = 10,
    val currentHp: Int = 10,
    val temporaryHp: Int = 0,
    val currentHitDice: Int = 1,
    val hitDieType: Int = 8, // e.g. 8 for d8
    
    // Death Saves
    val deathSaveSuccesses: Int = 0,
    val deathSaveFailures: Int = 0,
    
    // Conditions
    val exhaustionLevel: Int = 0,
    
    // Progression
    val experience: Int = 0,

    // Proficiencies
    val skillProficiencies: String = "",
    val savingThrowProficiencies: String = "",
    val weaponProficiencies: String = "",
    val armorProficiencies: String = "",
    val toolProficiencies: String = "",
    val languages: String = "",

    // Money
    val cp: Int = 0,
    val sp: Int = 0,
    val ep: Int = 0,
    val gp: Int = 0,
    val pp: Int = 0,

    // Spellcasting
    val spellcastingAbility: String = "INT",
    val usedSlots1: Int = 0,
    val usedSlots2: Int = 0,
    val usedSlots3: Int = 0,
    val usedSlots4: Int = 0,
    val usedSlots5: Int = 0,
    val usedSlots6: Int = 0,
    val usedSlots7: Int = 0,
    val usedSlots8: Int = 0,
    val usedSlots9: Int = 0,
    val preparesSpells: Boolean = true,
    val maxPreparedSpells: Int = 0,
    val maxKnownSpells: Int = 0,

    // Bonuses
    val initiativeBonus: Int = 0,
    val speedBonus: Int = 0,
    val baseSpeed: Int = 30,

    // Variant Rules / Config
    val useMilestones: Boolean = false,
    val useEncumbrance: Boolean = true,
    val hpCalculationMode: String = "Average", // "Max", "RollAboveAverage", "Roll", "Average"
    val useFeats: Boolean = false,
    val hasStartingFeat: Boolean = false,
    
    // Feats
    val featIndex: String = ""
)

@Entity(tableName = "character_equipment", primaryKeys = ["characterId", "equipmentIndex"])
data class CharacterEquipmentEntity(
    val characterId: Int,
    val equipmentIndex: String,
    val name: String = "",
    val quantity: Int = 1,
    val isEquipped: Boolean = false,
    val weight: Double = 0.0,
    val selectedAbility: String? = null,
    val useTwoHanded: Boolean = false
)

@Entity(tableName = "character_spells", primaryKeys = ["characterId", "spellIndex"])
data class CharacterSpellEntity(
    val characterId: Int,
    val spellIndex: String,
    val name: String = "",
    val customAbility: String? = null,
    val currentUses: Int = 0,
    val maxUses: Int = 0,
    val resetType: String = "Long Rest",
    val isPrepared: Boolean = true
)

@Entity(tableName = "character_trackers", primaryKeys = ["characterId", "name"])
data class CharacterTrackerEntity(
    val characterId: Int,
    val name: String,
    val current: Int,
    val max: Int,
    val resetType: String // "Short Rest" or "Long Rest"
)
