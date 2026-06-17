package com.emiwiana.forge5e.model.api.dto.character.characterClass
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable

/**
 * Data class representing a feature granted by a character class at a specific level.
 *
 * @property index Unique identifier for the class feature.
 * @property name Name of the class feature.
 * @property level The level at which this feature is gained.
 * @property desc Description of the class feature.
 * @property classReference Reference to the class that provides this feature.
 */
@Serializable
data class ClassFeature (
    val index: String,
    val name: String,
    val level: Int,
    val desc: List<String>,
    val classReference: APIReference? = null
)
