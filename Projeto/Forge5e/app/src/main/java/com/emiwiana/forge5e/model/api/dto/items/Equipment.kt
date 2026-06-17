package com.emiwiana.forge5e.model.api.dto.items

import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.mechanics.EquipmentQuantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a piece of equipment or item in D&D 5e.
 */
@Serializable
data class Equipment(
    val index: String,
    val name: String,
    @SerialName("equipment_category") val equipmentCategory: APIReference,
    @SerialName("weapon_category") val weaponCategory: String? = null,
    @SerialName("weapon_range") val weaponRange: String? = null,
    @SerialName("category_range") val categoryRange: String? = null,
    val range: Range? = null,
    @SerialName("armor_category") val armorCategory: String? = null,
    @SerialName("armor_class") val armorClass: ArmorClass? = null,
    @SerialName("str_minimum") val strMinimum: Int? = null,
    @SerialName("stealth_disadvantage") val stealthDisadvantage: Boolean? = null,
    val cost: Cost? = null,
    val damage: Damage? = null,
    val weight: Double? = null,
    val properties: List<APIReference>? = null,
    val contents: List<EquipmentQuantity>? = null,
    val desc: List<String>? = null
)

@Serializable
data class Range(
    val normal: Int,
    val long: Int? = null
)

@Serializable
data class ArmorClass(
    val base: Int,
    @SerialName("dex_bonus") val dexBonus: Boolean,
    @SerialName("max_bonus") val maxBonus: Int? = null
)

@Serializable
data class Cost(
    val quantity: Int,
    val unit: String
)

@Serializable
data class Damage(
    @SerialName("damage_dice") val damageDice: String,
    @SerialName("damage_type") val damageType: APIReference
)
