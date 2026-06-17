package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a Feat in D&D 5e.
 *
 * @property index Unique identifier for the feat.
 * @property name Name of the feat.
 * @property desc Description of the feat.
 * @property prerequisites List of prerequisites required to take this feat.
 */
@Serializable
data class Feat(
    val index: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<Prerequisite>? = null
)

/**
 * Data class representing a prerequisite for a feat.
 *
 * @property abilityScore Reference to the ability score required.
 * @property minimumScore The minimum score required for the specified ability.
 */
@Serializable
data class Prerequisite(
    @SerialName("ability_score") val abilityScore: APIReference? = null,
    @SerialName("minimum_score") val minimumScore: Int? = null
)
