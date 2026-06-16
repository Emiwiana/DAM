package com.emiwiana.forge5e.model.api.dto.magic

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val index: String,
    val name: String,
    val level: Int,
    val desc: List<String>,
    @SerialName("higher_level") val higherLevel: List<String>? = null,
    val range: String,
    val components: List<String>,
    val material: String? = null,
    val ritual: Boolean,
    val duration: String,
    val concentration: Boolean,
    @SerialName("casting_time") val castingTime: String,
    val school: APIReference // Often the API returns a nested reference object
)