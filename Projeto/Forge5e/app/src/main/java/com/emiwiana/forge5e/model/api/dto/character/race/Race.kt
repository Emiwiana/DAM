package com.emiwiana.forge5e.model.api.dto.character.race

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Race(
    val index: String,
    val name: String,
    val speed: Int,
    @SerialName("ability_bonuses") val abilityBonuses: List<AbilityBonus>,
    val languages: List<APIReference>,
    val traits: List<APIReference>
)

@Serializable
data class AbilityBonus(
    @SerialName("ability_score") val abilityScore: APIReference,
    val bonus: Int
)