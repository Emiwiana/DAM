package com.emiwiana.forge5e.model.api.dto.character.characterClass
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CharacterClass(
    val index: String,
    val name: String,
    @SerialName("hit_die") val hitDie: Int,
    @SerialName("saving_throws") val savingThrows: List<APIReference>,
    val proficiencies: List<APIReference>,
    @SerialName("class_levels") val classLevelsUrl: String
)