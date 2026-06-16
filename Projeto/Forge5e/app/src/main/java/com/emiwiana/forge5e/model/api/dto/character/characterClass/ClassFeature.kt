package com.emiwiana.forge5e.model.api.dto.character.characterClass
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable

@Serializable
data class ClassFeature (
    val index: String,
    val name: String,
    val level: Int,
    val desc: List<String>,
    val classReference: APIReference? = null
)