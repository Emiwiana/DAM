package com.emiwiana.forge5e.model.api.dto.character.race

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Subrace(
    val index: String,
    val name: String,
    @SerialName("race") val baseRace: APIReference,
    val desc: String? = null,
    @SerialName("ability_bonuses") val abilityBonuses: List<AbilityBonus>? = null,
    @SerialName("starting_proficiencies") val startingProficiencies: List<APIReference>? = null,
    @SerialName("racial_traits") val racialTraits: List<APIReference>? = null
)