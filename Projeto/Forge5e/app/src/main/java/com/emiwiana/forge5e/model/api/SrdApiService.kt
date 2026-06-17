package com.emiwiana.forge5e.model.api

import com.emiwiana.forge5e.model.api.dto.*
import com.emiwiana.forge5e.model.api.dto.character.characterClass.*
import com.emiwiana.forge5e.model.api.dto.character.race.Race
import com.emiwiana.forge5e.model.api.dto.character.race.RacialFeature
import com.emiwiana.forge5e.model.api.dto.character.race.Subrace
import com.emiwiana.forge5e.model.api.dto.items.Equipment
import com.emiwiana.forge5e.model.api.dto.magic.Spell
import com.emiwiana.forge5e.model.api.dto.mechanics.*
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for the D&D 5e System Reference Document (SRD) API.
 * Provides methods to fetch various game mechanics, classes, races, and more.
 */
interface SrdApiService {
    @GET("api/classes") suspend fun getClasses(): APIReferenceList
    @GET("api/races") suspend fun getRaces(): APIReferenceList
    @GET("api/spells") suspend fun getSpells(): APIReferenceList
    @GET("api/equipment") suspend fun getEquipmentList(): APIReferenceList
    @GET("api/feats") suspend fun getFeats(): APIReferenceList
    @GET("api/classes/{index}") suspend fun getClassDetails(@Path("index") index: String): CharacterClass
    @GET("api/races/{index}") suspend fun getRaceDetails(@Path("index") index: String): Race
    @GET("api/spells/{index}") suspend fun getSpellDetails(@Path("index") index: String): Spell
    @GET("api/equipment/{index}") suspend fun getEquipmentDetails(@Path("index") index: String): Equipment
    @GET("api/features/{index}") suspend fun getClassFeatureDetails(@Path("index") index: String): ClassFeature
    @GET("api/traits/{index}") suspend fun getRacialTraitDetails(@Path("index") index: String): RacialFeature
    @GET("api/feats/{index}") suspend fun getFeatDetails(@Path("index") index: String): Feat
    @GET("api/subclasses")
    suspend fun getSubclasses(): APIReferenceList
    @GET("api/subraces")
    suspend fun getSubraces(): APIReferenceList
    @GET("api/backgrounds")
    suspend fun getBackgrounds(): APIReferenceList
    @GET("api/skills")
    suspend fun getSkills(): APIReferenceList
    @GET("api/subclasses/{index}")
    suspend fun getSubclassDetails(@Path("index") index: String): Subclass
    @GET("api/subraces/{index}")
    suspend fun getSubraceDetails(@Path("index") index: String): Subrace
    @GET("api/backgrounds/{index}")
    suspend fun getBackgroundDetails(@Path("index") index: String): Background
    @GET("api/skills/{index}")
    suspend fun getSkillDetails(@Path("index") index: String): Skill
    @GET("api/classes/{index}/subclasses")
    suspend fun getClassSubclasses(@Path("index") index: String): APIReferenceList
    @GET("api/classes/{index}/features")
    suspend fun getClassFeatures(@Path("index") index: String): APIReferenceList
    @GET("api/classes/{index}/levels")
    suspend fun getClassLevels(@Path("index") index: String): List<ClassLevel>
    @GET("api/classes/{index}/spells")
    suspend fun getClassSpells(@Path("index") index: String): APIReferenceList
    @GET("api/subclasses/{index}/levels")
    suspend fun getSubclassLevels(@Path("index") index: String): List<ClassLevel>
    @GET("api/races/{index}/subraces")
    suspend fun getRaceSubraces(@Path("index") index: String): APIReferenceList
    @GET("api/races/{index}/traits")
    suspend fun getRaceTraits(@Path("index") index: String): APIReferenceList
    @GET("api/classes/{index}/starting-equipment")
    suspend fun getClassStartingEquipment(@Path("index") index: String): ClassStartingEquipment
    @GET("api/subclasses/{index}/features")
    suspend fun getSubclassFeatures(@Path("index") index: String): APIReferenceList
    @GET("api/subraces/{index}/traits")
    suspend fun getSubraceTraits(@Path("index") index: String): APIReferenceList
    
    @GET("api/proficiencies") suspend fun getProficiencies(): APIReferenceList
    @GET("api/proficiencies/{index}") suspend fun getProficiencyDetails(@Path("index") index: String): Proficiency
    @GET("api/proficiency-categories/{index}") suspend fun getProficiencyCategoryDetails(@Path("index") index: String): ProficiencyCategory
}
