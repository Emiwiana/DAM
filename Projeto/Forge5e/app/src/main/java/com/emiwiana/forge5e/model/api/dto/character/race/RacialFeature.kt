package com.emiwiana.forge5e.model.api.dto.character.race
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable

/**
 * Data class representing a racial feature in D&D 5e.
 *
 * @property index Unique identifier for the racial feature.
 * @property name Name of the racial feature.
 * @property desc Description of the racial feature.
 * @property races List of races that possess this feature.
 */
@Serializable
data class RacialFeature (
    val index: String,
    val name: String,
    val desc: List<String>,
    val races: List<APIReference>? = null
)
