package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Skill(
    val index: String,
    val name: String,
    val desc: List<String>,
    @SerialName("ability_score") val baseAbilityScore: APIReference
)