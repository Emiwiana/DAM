package com.emiwiana.forge5e.model.repository

import com.emiwiana.forge5e.model.api.SrdApiService
import com.emiwiana.forge5e.model.api.dto.*
import com.emiwiana.forge5e.model.api.dto.character.characterClass.CharacterClass
import com.emiwiana.forge5e.model.api.dto.character.characterClass.ClassFeature
import com.emiwiana.forge5e.model.api.dto.character.characterClass.Subclass
import com.emiwiana.forge5e.model.api.dto.character.race.Race
import com.emiwiana.forge5e.model.api.dto.character.race.RacialFeature
import com.emiwiana.forge5e.model.api.dto.character.race.Subrace
import com.emiwiana.forge5e.model.api.dto.items.Equipment
import com.emiwiana.forge5e.model.api.dto.magic.Spell
import com.emiwiana.forge5e.model.api.dto.mechanics.Background
import com.emiwiana.forge5e.model.api.dto.mechanics.Feat
import com.emiwiana.forge5e.model.api.dto.mechanics.Skill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SrdRepository(private val apiService: SrdApiService) {

    // --- Detail Fetchers ---
    suspend fun fetchSpell(index: String): Result<Spell> = withContext(Dispatchers.IO) { runCatching { apiService.getSpellDetails(index) } }
    suspend fun fetchCharacterClass(index: String): Result<CharacterClass> = withContext(Dispatchers.IO) { runCatching { apiService.getClassDetails(index) } }
    suspend fun fetchRace(index: String): Result<Race> = withContext(Dispatchers.IO) { runCatching { apiService.getRaceDetails(index) } }
    suspend fun fetchEquipment(index: String): Result<Equipment> = withContext(Dispatchers.IO) { runCatching { apiService.getEquipmentDetails(index) } }
    suspend fun fetchFeat(index: String): Result<Feat> = withContext(Dispatchers.IO) { runCatching { apiService.getFeatDetails(index) } }
    suspend fun fetchSubclass(index: String): Result<Subclass> = withContext(Dispatchers.IO) { runCatching { apiService.getSubclassDetails(index) } }
    suspend fun fetchSubrace(index: String): Result<Subrace> = withContext(Dispatchers.IO) { runCatching { apiService.getSubraceDetails(index) } }
    suspend fun fetchBackground(index: String): Result<Background> = withContext(Dispatchers.IO) { runCatching { apiService.getBackgroundDetails(index) } }
    suspend fun fetchSkill(index: String): Result<Skill> = withContext(Dispatchers.IO) { runCatching { apiService.getSkillDetails(index) } }
    suspend fun fetchClassFeature(index: String): Result<ClassFeature> = withContext(Dispatchers.IO) { runCatching { apiService.getClassFeatureDetails(index) } }
    suspend fun fetchRacialFeature(index: String): Result<RacialFeature> = withContext(Dispatchers.IO) { runCatching { apiService.getRacialTraitDetails(index) } }

    // --- List Fetchers ---
    suspend fun fetchAllAvailableSpells(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getSpells() } }
    suspend fun fetchAllAvailableClasses(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getClasses() } }
    suspend fun fetchAllAvailableRaces(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getRaces() } }
    suspend fun fetchAllAvailableFeats(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getFeats() } }
    suspend fun fetchAllAvailableEquipment(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getEquipmentList() } }
    suspend fun fetchAllAvailableSubclasses(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getSubclasses() } }
    suspend fun fetchAllAvailableBackgrounds(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getBackgrounds() } }
    suspend fun fetchAllAvailableSkills(): Result<APIReferenceList> = withContext(Dispatchers.IO) { runCatching { apiService.getSkills() } }

    // --- Contextual Fetchers ---
    suspend fun fetchClassSubclasses(index: String) = withContext(Dispatchers.IO) { runCatching { apiService.getClassSubclasses(index) } }
    suspend fun fetchClassFeatures(index: String) = withContext(Dispatchers.IO) { runCatching { apiService.getClassFeatures(index) } }
    suspend fun fetchRaceSubraces(index: String) = withContext(Dispatchers.IO) { runCatching { apiService.getRaceSubraces(index) } }
    suspend fun fetchRaceTraits(index: String) = withContext(Dispatchers.IO) { runCatching { apiService.getRaceTraits(index) } }
}
