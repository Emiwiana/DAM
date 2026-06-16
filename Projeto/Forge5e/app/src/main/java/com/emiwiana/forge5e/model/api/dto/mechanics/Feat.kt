package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Feat(
    val index: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<Prerequisite>? = null
)

@Serializable
data class Prerequisite(
    @SerialName("ability_score") val abilityScore: APIReference? = null,
    @SerialName("minimum_score") val minimumScore: Int? = null
)