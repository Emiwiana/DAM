package com.emiwiana.forge5e.model.domain

import com.emiwiana.forge5e.model.api.dto.APIReference

data class EquipmentItem(
    override val id: String,
    override val name: String,
    val equipmentCategory: String, // e.g., "Martial Melee Weapons", "Shield"
    val cost: String,              // e.g., "15 gp"
    val weight: Double,            // e.g., 4.0 (lbs)
    override val description: String,
    val damage: String?,           // e.g., "1d8 slashing"
    val properties: List<String>,  // e.g., ["Finesse", "Light", "Thrown"]
    override val isHomebrew: Boolean,
    val contents: List<EquipmentContent> = emptyList()
) : IBrowserItem {
    // Hardcode the BrowseCategory for the UI layout/icons
    override val category: BrowseCategory = BrowseCategory.EQUIPMENT

    // Dynamically build the subtitle for the UI tooltip
    override val subtitle: String
        get() = "$equipmentCategory • $cost • $weight lbs"

    // Dynamically build the metadata map for the UI
    override val metadata: Map<String, String>
        get() = buildMap {
            damage?.let { put("Damage", it) }
            if (properties.isNotEmpty()) {
                put("Properties", properties.joinToString(", "))
            }
        }
}

data class EquipmentContent(
    val item: APIReference,
    val quantity: Int
)
