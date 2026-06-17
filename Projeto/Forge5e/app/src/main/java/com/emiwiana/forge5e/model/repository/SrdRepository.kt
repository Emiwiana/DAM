package com.emiwiana.forge5e.model.repository

import com.emiwiana.forge5e.model.api.SrdApiService
import com.emiwiana.forge5e.model.api.dto.*
import com.emiwiana.forge5e.model.api.dto.character.characterClass.*
import com.emiwiana.forge5e.model.api.dto.character.race.*
import com.emiwiana.forge5e.model.api.dto.items.Equipment
import com.emiwiana.forge5e.model.api.dto.magic.Spell
import com.emiwiana.forge5e.model.api.dto.mechanics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class that handles data operations for the D&D 5e SRD.
 * It abstracts the API calls and provides data to the ViewModels using [Result] wrappers.
 *
 * @property apiService The Retrofit API service used to fetch data.
 */
class SrdRepository(private val apiService: SrdApiService) {

    /**
     * Executes an API call on the IO dispatcher and wraps the result in a [Result].
     */
    private suspend fun <T> apiCall(call: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) { runCatching { call() } }

    // --- Detail Fetchers ---
    
    suspend fun fetchSpell(index: String) = apiCall { apiService.getSpellDetails(index) }
    suspend fun fetchCharacterClass(index: String) = apiCall { apiService.getClassDetails(index) }
    suspend fun fetchRace(index: String) = apiCall { apiService.getRaceDetails(index) }
    suspend fun fetchEquipment(index: String) = apiCall { apiService.getEquipmentDetails(index) }
    suspend fun fetchFeat(index: String) = apiCall { apiService.getFeatDetails(index) }
    suspend fun fetchSubclass(index: String) = apiCall { apiService.getSubclassDetails(index) }
    suspend fun fetchSubrace(index: String) = apiCall { apiService.getSubraceDetails(index) }
    suspend fun fetchBackground(index: String) = apiCall { apiService.getBackgroundDetails(index) }
    suspend fun fetchSkill(index: String) = apiCall { apiService.getSkillDetails(index) }
    suspend fun fetchClassFeature(index: String) = apiCall { apiService.getClassFeatureDetails(index) }
    suspend fun fetchRacialFeature(index: String) = apiCall { apiService.getRacialTraitDetails(index) }

    // --- List Fetchers ---
    
    suspend fun fetchAllAvailableSpells() = apiCall { apiService.getSpells() }
    suspend fun fetchAllAvailableClasses() = apiCall { apiService.getClasses() }
    suspend fun fetchAllAvailableRaces() = apiCall { apiService.getRaces() }
    suspend fun fetchAllAvailableFeats() = apiCall { apiService.getFeats() }
    suspend fun fetchAllAvailableEquipment() = apiCall { apiService.getEquipmentList() }
    suspend fun fetchAllAvailableSubclasses() = apiCall { apiService.getSubclasses() }
    suspend fun fetchAllAvailableBackgrounds() = apiCall { apiService.getBackgrounds() }
    suspend fun fetchAllAvailableSkills() = apiCall { apiService.getSkills() }

    // --- Contextual Fetchers ---

    suspend fun fetchClassSubclasses(index: String) = apiCall { apiService.getClassSubclasses(index) }
    suspend fun fetchClassFeatures(index: String) = apiCall { apiService.getClassFeatures(index) }
    suspend fun fetchRaceSubraces(index: String) = apiCall { apiService.getRaceSubraces(index) }
    suspend fun fetchRaceTraits(index: String) = apiCall { apiService.getRaceTraits(index) }
    suspend fun fetchClassStartingEquipment(index: String) = apiCall { apiService.getClassStartingEquipment(index) }
    suspend fun fetchSubclassFeatures(index: String) = apiCall { apiService.getSubclassFeatures(index) }
    suspend fun fetchSubraceTraits(index: String) = apiCall { apiService.getSubraceTraits(index) }
}
