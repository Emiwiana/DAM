package com.emiwiana.forge5e.model.api.dto
import kotlinx.serialization.Serializable

/**
 * Data class representing a reference to another object in the D&D 5e API.
 *
 * @property index Unique identifier for the referenced object.
 * @property name Name of the referenced object.
 * @property url Relative URL to the detailed information of the referenced object.
 */
@Serializable
data class APIReference(
    val index: String,
    val name: String,
    val url: String
)
