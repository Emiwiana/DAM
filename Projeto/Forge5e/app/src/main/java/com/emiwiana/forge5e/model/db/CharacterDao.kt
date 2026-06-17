package com.emiwiana.forge5e.model.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

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

    @Delete
    suspend fun deleteSpell(spell: CharacterSpellEntity)
}
