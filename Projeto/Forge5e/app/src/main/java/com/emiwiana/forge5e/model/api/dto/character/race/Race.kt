package com.emiwiana.forge5e.model.api.dto.character.race

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a Race in D&D 5e.
 *
 * @property index Unique identifier for the race.
 * @property name Name of the race.
 * @property speed Base walking speed for this race.
 * @property abilityBonuses List of ability score bonuses provided by this race.
 * @property languages List of languages typically known by members of this race.
 * @property traits List of racial traits for this race.
 */
@Serializable
data class Race(
    val index: String,
    val name: String,
    val speed: Int,
    @SerialName("ability_bonuses") val abilityBonuses: List<AbilityBonus>,
    val languages: List<APIReference>,
    val traits: List<APIReference>
)

/**
 * Data class representing an ability score bonus.
 *
 * @property abilityScore Reference to the ability score that receives the bonus.
 * @property bonus The numerical value of the bonus.
 */
@Serializable
data class AbilityBonus(
    @SerialName("ability_score") val abilityScore: APIReference,
    val bonus: Int
)
