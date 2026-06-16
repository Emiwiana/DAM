package com.emiwiana.forge5e.model.api.dto
import kotlinx.serialization.Serializable

@Serializable
data class APIReferenceList(
    val count: Int,
    val results: List<APIReference>
)