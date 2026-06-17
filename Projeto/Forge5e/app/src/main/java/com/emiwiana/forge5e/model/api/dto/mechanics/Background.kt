package com.emiwiana.forge5e.model.api.dto.mechanics

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a character background in D&D 5e.
 *
 * @property index Unique identifier for the background.
 * @property name Name of the background.
 * @property startingProficiencies List of proficiencies granted by this background.
 * @property backgroundFeature The specific feature granted by this background.
 * @property startingEquipment List of equipment and their quantities granted by this background.
 */
@Serializable
data class Background(
    val index: String,
    val name: String,
    @SerialName("starting_proficiencies") val startingProficiencies: List<APIReference>? = null,
    @SerialName("feature") val backgroundFeature: BackgroundFeature,
    @SerialName("starting_equipment") val startingEquipment: List<EquipmentQuantity>? = null
)

/**
 * Data class representing a feature granted by a background.
 *
 * @property name Name of the background feature.
 * @property desc Description of the background feature.
 */
@Serializable
data class BackgroundFeature(
    val name: String,
    val desc: List<String>
)

/**
 * Data class representing a quantity of a specific piece of equipment or item.
 *
 * @property quantity The number of items.
 * @property equipment Reference to the equipment.
 * @property item Reference to the item.
 */
@Serializable
data class EquipmentQuantity(
    val quantity: Int,
    val equipment: APIReference? = null,
    val item: APIReference? = null
)
