package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Proficiency(
    val index: String,
    val type: String,
    val name: String,
    val classes: List<APIReference>? = null,
    val races: List<APIReference>? = null,
    val url: String,
    val reference: APIReference? = null
)

@Serializable
data class ProficiencyCategory(
    val index: String,
    val name: String,
    val proficiencies: List<APIReference>,
    val url: String
)
