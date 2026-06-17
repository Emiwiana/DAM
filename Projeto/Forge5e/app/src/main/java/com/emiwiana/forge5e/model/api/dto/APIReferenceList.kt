package com.emiwiana.forge5e.model.api.dto
import kotlinx.serialization.Serializable

/**
 * Data class representing a list of references from the D&D 5e API.
 * This is usually the top-level response for collection endpoints.
 *
 * @property count The number of items in the list.
 * @property results The list of API references.
 */
@Serializable
data class APIReferenceList(
    val count: Int,
    val results: List<APIReference>
)
