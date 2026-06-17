package com.emiwiana.forge5e.model.api.dto.character.characterClass

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable

@Serializable
data class ClassLevel(
    val level: Int,
    val features: List<APIReference>
)
