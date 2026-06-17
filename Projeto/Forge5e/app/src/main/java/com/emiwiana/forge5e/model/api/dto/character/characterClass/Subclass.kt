package com.emiwiana.forge5e.model.api.dto.character.characterClass

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a Subclass in D&D 5e (e.g., Berserker for Barbarian).
 *
 * @property index Unique identifier for the subclass.
 * @property name Name of the subclass.
 * @property baseClass Reference to the parent class.
 * @property desc Description of the subclass.
 * @property features List of features granted by this subclass.
 */
@Serializable
data class Subclass(
    val index: String,
    val name: String,
    @SerialName("class") val baseClass: APIReference,
    val desc: List<String>,
    val features: List<APIReference>? = null
)
