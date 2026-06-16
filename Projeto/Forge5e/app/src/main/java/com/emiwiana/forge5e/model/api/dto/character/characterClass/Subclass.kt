package com.emiwiana.forge5e.model.api.dto.character.characterClass

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Subclass(
    val index: String,
    val name: String,
    @SerialName("class") val baseClass: APIReference,
    val desc: List<String>,
    val features: List<APIReference>? = null
)