package com.emiwiana.forge5e.model.api.dto.character.characterClass

import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.mechanics.EquipmentQuantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CharacterClass(
    val index: String,
    val name: String,
    @SerialName("hit_die") val hitDie: Int,
    @SerialName("saving_throws") val savingThrows: List<APIReference>,
    val proficiencies: List<APIReference>,
    @SerialName("proficiency_choices") val proficiencyChoices: List<ProficiencyChoice>? = null,
    @SerialName("class_levels") val classLevelsUrl: String,
    val spellcasting: ClassSpellcasting? = null
)

@Serializable
data class ProficiencyChoice(
    val desc: String? = null,
    val choose: Int,
    val type: String,
    val from: ProficiencyOptionSet
)

@Serializable
data class ProficiencyOptionSet(
    @SerialName("option_set_type") val optionSetType: String,
    val options: List<ProficiencyOption>? = null,
    @SerialName("proficiency_category") val proficiencyCategory: APIReference? = null
)

@Serializable
data class ProficiencyOption(
    @SerialName("option_type") val optionType: String,
    val item: APIReference? = null,
    val choice: ProficiencyChoice? = null
)

@Serializable
data class ClassSpellcasting(
    val level: Int,
    @SerialName("spellcasting_ability") val spellcastingAbility: APIReference,
    val info: List<SpellcastingInfo>
)

@Serializable
data class SpellcastingInfo(
    val name: String,
    val desc: List<String>
)

@Serializable
data class ClassStartingEquipment(
    @SerialName("starting_equipment") val startingEquipment: List<EquipmentQuantity>,
    @SerialName("starting_equipment_options") val startingEquipmentOptions: List<EquipmentChoice>? = null
)

@Serializable
data class EquipmentChoice(
    val desc: String? = null,
    val choose: Int,
    val type: String,
    val from: EquipmentOptionSet
)

@Serializable
data class EquipmentOptionSet(
    @SerialName("option_set_type") val optionSetType: String,
    val options: List<EquipmentOption>? = null,
    @SerialName("equipment_category") val equipmentCategory: APIReference? = null
)

@Serializable
data class EquipmentOption(
    @SerialName("option_type") val optionType: String,
    val item: APIReference? = null,
    val count: Int? = null,
    val choice: EquipmentChoice? = null,
    val of: APIReference? = null,
    val items: List<EquipmentOption>? = null
)
