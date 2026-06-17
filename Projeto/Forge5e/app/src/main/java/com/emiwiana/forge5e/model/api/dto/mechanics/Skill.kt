package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a Skill in D&D 5e.
 *
 * @property index Unique identifier for the skill.
 * @property name Name of the skill.
 * @property desc Description of the skill.
 * @property baseAbilityScore Reference to the ability score that this skill is based on.
 */
@Serializable
data class Skill(
    val index: String,
    val name: String,
    val desc: List<String>,
    @SerialName("ability_score") val baseAbilityScore: APIReference
)
