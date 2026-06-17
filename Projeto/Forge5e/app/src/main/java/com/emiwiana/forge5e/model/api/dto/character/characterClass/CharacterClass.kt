package com.emiwiana.forge5e.model.api.dto.character.characterClass

import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.mechanics.EquipmentQuantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a Character Class in D&D 5e (e.g., Fighter, Wizard).
 *
 * @property index Unique identifier for the class.
 * @property name Name of the class.
 * @property hitDie The hit die of the class (e.g., 8 for d8, 10 for d10).
 * @property savingThrows List of ability scores this class is proficient in for saving throws.
 * @property proficiencies List of proficiencies granted by this class.
 * @property classLevelsUrl Relative URL to the levels information for this class.
 */
@Serializable
data class CharacterClass(
    val index: String,
    val name: String,
    @SerialName("hit_die") val hitDie: Int,
    @SerialName("saving_throws") val savingThrows: List<APIReference>,
    val proficiencies: List<APIReference>,
    @SerialName("class_levels") val classLevelsUrl: String
)

/**
 * Data class representing the starting equipment for a character class.
 *
 * @property startingEquipment List of fixed equipment granted at level 1.
 * @property startingEquipmentOptions List of equipment choices offered to the character.
 */
@Serializable
data class ClassStartingEquipment(
    @SerialName("starting_equipment") val startingEquipment: List<EquipmentQuantity>,
    @SerialName("starting_equipment_options") val startingEquipmentOptions: List<EquipmentChoice>? = null
)

/**
 * Data class representing a choice of equipment.
 *
 * @property desc Description of the choice.
 * @property choose Number of options to choose.
 * @property type Type of the choice.
 * @property from The set of options to choose from.
 */
@Serializable
data class EquipmentChoice(
    val desc: String? = null,
    val choose: Int,
    val type: String,
    val from: EquipmentOptionSet
)

/**
 * Data class representing a set of equipment options.
 *
 * @property optionSetType The type of option set (e.g., "options_array", "equipment_category").
 * @property options List of specific options if available.
 * @property equipmentCategory Reference to an equipment category if the choice is from a category.
 */
@Serializable
data class EquipmentOptionSet(
    @SerialName("option_set_type") val optionSetType: String,
    val options: List<EquipmentOption>? = null,
    @SerialName("equipment_category") val equipmentCategory: APIReference? = null
)

/**
 * Data class representing a specific option within an equipment choice.
 *
 * @property optionType The type of option (e.g., "counted_reference", "choice").
 * @property item Reference to a specific item.
 * @property count The number of items.
 * @property choice A nested choice if this option leads to another choice.
 * @property of Reference to what this option is part of.
 * @property items List of items if this option includes multiple items.
 */
@Serializable
data class EquipmentOption(
    @SerialName("option_type") val optionType: String,
    val item: APIReference? = null,
    val count: Int? = null,
    val choice: EquipmentChoice? = null,
    val of: APIReference? = null,
    val items: List<EquipmentOption>? = null
)
