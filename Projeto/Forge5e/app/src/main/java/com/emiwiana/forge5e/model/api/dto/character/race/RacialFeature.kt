package com.emiwiana.forge5e.model.api.dto.character.race
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable

@Serializable
data class RacialFeature (
    val index: String,
    val name: String,
    val desc: List<String>,
    val races: List<APIReference>? = null
)