package com.emiwiana.forge5e.model.api.dto.items

import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.mechanics.EquipmentQuantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a piece of equipment or item in D&D 5e.
 *
 * @property index Unique identifier for the equipment.
 * @property name Name of the equipment.
 * @property equipmentCategory Reference to the category of equipment.
 * @property weaponCategory Category of weapon (e.g., Simple, Martial), if applicable.
 * @property weaponRange Range type of the weapon (e.g., Melee, Ranged), if applicable.
 * @property categoryRange Range category, if applicable.
 * @property cost The cost of the item.
 * @property damage Damage details if the item is a weapon.
 * @property weight Weight of the item in pounds.
 * @property properties List of special properties (e.g., Finesse, Versatile).
 * @property contents List of items contained within (e.g., for a dungeoneer's pack).
 * @property desc Description of the item or its special effects.
 */
@Serializable
data class Equipment(
    val index: String,
    val name: String,
    @SerialName("equipment_category") val equipmentCategory: APIReference,
    @SerialName("weapon_category") val weaponCategory: String? = null,
    @SerialName("weapon_range") val weaponRange: String? = null,
    @SerialName("category_range") val categoryRange: String? = null,
    val cost: Cost? = null,
    val damage: Damage? = null,
    val weight: Double? = null,
    val properties: List<APIReference>? = null,
    val contents: List<EquipmentQuantity>? = null,
    val desc: List<String>? = null
)

/**
 * Data class representing the cost of an item.
 *
 * @property quantity The numerical cost.
 * @property unit The currency unit (e.g., "gp", "sp", "cp").
 */
@Serializable
data class Cost(
    val quantity: Int,
    val unit: String
)

/**
 * Data class representing damage information for a weapon.
 *
 * @property damageDice The dice used for damage (e.g., "1d8").
 * @property damageType Reference to the type of damage (e.g., Slashing).
 */
@Serializable
data class Damage(
    @SerialName("damage_dice") val damageDice: String,
    @SerialName("damage_type") val damageType: APIReference
)
