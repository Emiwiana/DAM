package com.emiwiana.forge5e.model.api.dto.items
import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    val properties: List<APIReference>? = null
)

@Serializable
data class Cost(
    val quantity: Int,
    val unit: String // e.g., "gp", "sp", "cp"
)

@Serializable
data class Damage(
    @SerialName("damage_dice") val damageDice: String, // e.g., "1d8"
    @SerialName("damage_type") val damageType: APIReference
)