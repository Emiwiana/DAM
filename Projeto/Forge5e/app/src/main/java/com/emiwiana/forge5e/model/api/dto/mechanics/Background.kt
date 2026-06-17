package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Background(
    val index: String,
    val name: String,
    @SerialName("starting_proficiencies") val startingProficiencies: List<APIReference>? = null,
    @SerialName("feature") val backgroundFeature: BackgroundFeature,
    @SerialName("starting_equipment") val startingEquipment: List<EquipmentQuantity>? = null
)

@Serializable
data class BackgroundFeature(
    val name: String,
    val desc: List<String>
)

@Serializable
data class EquipmentQuantity(
    val quantity: Int,
    val equipment: APIReference? = null,
    val item: APIReference? = null
)
