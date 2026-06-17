package com.emiwiana.forge5e.model.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterById(id: Int): Flow<CharacterEntity?>

    // Equipment
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: CharacterEquipmentEntity)

    @Query("SELECT * FROM character_equipment WHERE characterId = :characterId")
    fun getCharacterEquipment(characterId: Int): Flow<List<CharacterEquipmentEntity>>

    @Update
    suspend fun updateEquipment(equipment: CharacterEquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: CharacterEquipmentEntity)

    // Spells
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpell(spell: CharacterSpellEntity)

    @Query("SELECT * FROM character_spells WHERE characterId = :characterId")
    fun getCharacterSpells(characterId: Int): Flow<List<CharacterSpellEntity>>

    @Update
    suspend fun updateSpell(spell: CharacterSpellEntity)

    @Delete
    suspend fun deleteSpell(spell: CharacterSpellEntity)

    // Trackers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: CharacterTrackerEntity)

    @Query("SELECT * FROM character_trackers WHERE characterId = :characterId")
    fun getCharacterTrackers(characterId: Int): Flow<List<CharacterTrackerEntity>>

    @Update
    suspend fun updateTracker(tracker: CharacterTrackerEntity)

    @Delete
    suspend fun deleteTracker(tracker: CharacterTrackerEntity)

    // Features
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: CharacterFeatureEntity)

    @Query("SELECT * FROM character_features WHERE characterId = :characterId")
    fun getCharacterFeatures(characterId: Int): Flow<List<CharacterFeatureEntity>>

    @Query("DELETE FROM character_features WHERE characterId = :characterId")
    suspend fun deleteCharacterFeatures(characterId: Int)

    @Delete
    suspend fun deleteFeature(feature: CharacterFeatureEntity)

    @Transaction
    suspend fun syncFeatures(characterId: Int, features: List<CharacterFeatureEntity>) {
        deleteCharacterFeatures(characterId)
        features.forEach { insertFeature(it) }
    }
}
