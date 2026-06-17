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
    val race: String = "",
    val raceIndex: String = "",
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
    
    // Death Saves
    val deathSaveSuccesses: Int = 0,
    val deathSaveFailures: Int = 0,
    
    // Conditions
    val exhaustionLevel: Int = 0,
    
    // Progression
    val experience: Int = 0,

    // Proficiencies (storing as comma-separated strings for simplicity in this schema)
    val skillProficiencies: String = "",
    val savingThrowProficiencies: String = ""
)

@Entity(tableName = "character_equipment", primaryKeys = ["characterId", "equipmentIndex"])
data class CharacterEquipmentEntity(
    val characterId: Int,
    val equipmentIndex: String,
    val name: String = "",
    val quantity: Int = 1,
    val isEquipped: Boolean = false,
    val weight: Double = 0.0
)

@Entity(tableName = "character_spells", primaryKeys = ["characterId", "spellIndex"])
data class CharacterSpellEntity(
    val characterId: Int,
    val spellIndex: String,
    val name: String = ""
)
