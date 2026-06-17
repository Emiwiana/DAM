package com.emiwiana.forge5e.model.api.dto.character.race

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a Subrace in D&D 5e.
 *
 * @property index Unique identifier for the subrace.
 * @property name Name of the subrace.
 * @property baseRace Reference to the parent race.
 * @property desc Description of the subrace.
 * @property abilityBonuses List of ability score bonuses provided by this subrace.
 * @property startingProficiencies List of proficiencies granted by this subrace.
 * @property racialTraits List of racial traits for this subrace.
 */
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
