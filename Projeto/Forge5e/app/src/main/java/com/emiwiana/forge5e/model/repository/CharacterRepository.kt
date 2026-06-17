package com.emiwiana.forge5e.model.repository

import com.emiwiana.forge5e.model.db.*
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val characterDao: CharacterDao) {
    val allCharacters: Flow<List<CharacterEntity>> = characterDao.getAllCharacters()

    suspend fun insert(character: CharacterEntity) {
        characterDao.insertCharacter(character)
    }

    suspend fun delete(character: CharacterEntity) {
        characterDao.deleteCharacter(character)
    }

    fun getCharacterById(id: Int): Flow<CharacterEntity?> {
        return characterDao.getCharacterById(id)
    }

    // Equipment
    fun getCharacterEquipment(characterId: Int): Flow<List<CharacterEquipmentEntity>> {
        return characterDao.getCharacterEquipment(characterId)
    }

    suspend fun addEquipment(equipment: CharacterEquipmentEntity) {
        characterDao.insertEquipment(equipment)
    }

    suspend fun updateEquipment(equipment: CharacterEquipmentEntity) {
        characterDao.updateEquipment(equipment)
    }

    suspend fun removeEquipment(equipment: CharacterEquipmentEntity) {
        characterDao.deleteEquipment(equipment)
    }

    // Spells
    fun getCharacterSpells(characterId: Int): Flow<List<CharacterSpellEntity>> {
        return characterDao.getCharacterSpells(characterId)
    }

    suspend fun addSpell(spell: CharacterSpellEntity) {
        characterDao.insertSpell(spell)
    }

    suspend fun updateSpell(spell: CharacterSpellEntity) {
        characterDao.updateSpell(spell)
    }

    suspend fun removeSpell(spell: CharacterSpellEntity) {
        characterDao.deleteSpell(spell)
    }

    // Trackers
    fun getCharacterTrackers(characterId: Int): Flow<List<CharacterTrackerEntity>> {
        return characterDao.getCharacterTrackers(characterId)
    }

    suspend fun addTracker(tracker: CharacterTrackerEntity) {
        characterDao.insertTracker(tracker)
    }

    suspend fun updateTracker(tracker: CharacterTrackerEntity) {
        characterDao.updateTracker(tracker)
    }

    suspend fun removeTracker(tracker: CharacterTrackerEntity) {
        characterDao.deleteTracker(tracker)
    }
}
