package com.emiwiana.forge5e.model.api.dto.magic

import com.emiwiana.forge5e.model.api.dto.APIReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a Spell in D&D 5e.
 *
 * @property index Unique identifier for the spell.
 * @property name Name of the spell.
 * @property level Level of the spell (0 for cantrips).
 * @property desc Description of the spell's effects.
 * @property higherLevel Description of the spell's effects when cast at a higher level.
 * @property range Range of the spell.
 * @property components List of required components (V, S, M).
 * @property material Textual description of the material components required.
 * @property ritual Whether the spell can be cast as a ritual.
 * @property duration Duration of the spell.
 * @property concentration Whether the spell requires concentration.
 * @property castingTime Time required to cast the spell.
 * @property school Reference to the magic school this spell belongs to.
 */
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
    val school: APIReference
)
