package com.emiwiana.forge5e.model.domain

import com.emiwiana.forge5e.model.api.dto.APIReference

/**
 * Domain model representing a piece of equipment or item.
 * Implements [IBrowserItem] for display in the SRD browser.
 *
 * @property id Unique identifier for the equipment.
 * @property name Name of the equipment.
 * @property equipmentCategory Category of the equipment (e.g., "Martial Melee Weapons").
 * @property cost Textual representation of the cost (e.g., "15 gp").
 * @property weight Weight of the item in pounds.
 * @property description Detailed description of the item.
 * @property damage Damage information if the item is a weapon (e.g., "1d8 slashing").
 * @property properties List of special properties (e.g., "Finesse").
 * @property isHomebrew Whether the item is user-created content.
 * @property contents List of items contained within this item.
 */
data class EquipmentItem(
    override val id: String,
    override val name: String,
    val equipmentCategory: String,
    val cost: String,
    val weight: Double,
    override val description: String,
    val damage: String?,
    val properties: List<String>,
    override val isHomebrew: Boolean,
    val contents: List<EquipmentContent> = emptyList()
) : IBrowserItem {
    
    override val category: BrowseCategory = BrowseCategory.EQUIPMENT

    override val subtitle: String
        get() = "$equipmentCategory • $cost • $weight lbs"

    override val metadata: Map<String, String>
        get() = buildMap {
            damage?.let { put("Damage", it) }
            if (properties.isNotEmpty()) {
                put("Properties", properties.joinToString(", "))
            }
        }
}

/**
 * Represents a specific item and its quantity contained within another piece of equipment.
 *
 * @property item Reference to the contained item.
 * @property quantity Number of items included.
 */
data class EquipmentContent(
    val item: APIReference,
    val quantity: Int
)
