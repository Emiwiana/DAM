package com.emiwiana.forge5e.model.api.dto
import kotlinx.serialization.Serializable

@Serializable
data class APIReference(
    val index: String,
    val name: String,
    val url: String
)