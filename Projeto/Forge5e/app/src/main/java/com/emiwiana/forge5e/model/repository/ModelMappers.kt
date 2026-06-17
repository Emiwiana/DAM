package com.emiwiana.forge5e.model.repository

import com.emiwiana.forge5e.model.api.dto.character.characterClass.CharacterClass
import com.emiwiana.forge5e.model.api.dto.character.characterClass.ClassFeature
import com.emiwiana.forge5e.model.api.dto.character.characterClass.Subclass
import com.emiwiana.forge5e.model.api.dto.character.race.Race
import com.emiwiana.forge5e.model.api.dto.character.race.RacialFeature
import com.emiwiana.forge5e.model.api.dto.character.race.Subrace
import com.emiwiana.forge5e.model.api.dto.items.Equipment
import com.emiwiana.forge5e.model.api.dto.magic.Spell
import com.emiwiana.forge5e.model.api.dto.mechanics.Background
import com.emiwiana.forge5e.model.api.dto.mechanics.Feat
import com.emiwiana.forge5e.model.domain.BrowseCategory
import com.emiwiana.forge5e.model.domain.BrowserItem
import com.emiwiana.forge5e.model.domain.EquipmentContent
import com.emiwiana.forge5e.model.domain.EquipmentItem

/**
 * Mapper extension functions to convert API DTOs into clean domain models.
 * This separation ensures the UI doesn't depend directly on the network implementation.
 */

// ==========================================
// 1. MAGIC & MECHANICS MAPPERS
// ==========================================

/**
 * Converts a [Spell] DTO to a [BrowserItem] domain model.
 */
fun Spell.toDomainModel(): BrowserItem {
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.SPELL,
        subtitle = "Level ${this.level} ${this.school.name}",
        description = this.desc.joinToString("\n\n"),
        metadata = mapOf(
            "Casting Time" to this.castingTime,
            "Range" to this.range,
            "Duration" to this.duration,
            "Concentration" to if (this.concentration) "Yes" else "No",
            "Ritual" to if (this.ritual) "Yes" else "No"
        ),
        isHomebrew = false
    )
}

/**
 * Converts a [Feat] DTO to a [BrowserItem] domain model.
 */
fun Feat.toDomainModel(): BrowserItem {
    val prereqs = this.prerequisites?.mapNotNull {
        if (it.abilityScore != null && it.minimumScore != null) {
            "${it.abilityScore.name} ${it.minimumScore}+"
        } else null
    }?.joinToString(", ") ?: "None"

    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.FEAT,
        subtitle = "Prerequisite: $prereqs",
        description = this.desc.joinToString("\n\n"),
        isHomebrew = false
    )
}

/**
 * Converts a [Background] DTO to a [BrowserItem] domain model.
 */
fun Background.toDomainModel(): BrowserItem {
    val proficiencies = this.startingProficiencies?.joinToString { it.name } ?: "None"

    val equipment = this.startingEquipment?.joinToString {
        "${it.quantity}x ${it.equipment?.name ?: it.item?.name ?: "Unknown"}"
    } ?: "None"

    val featureHeading = "**Background Feature: ${this.backgroundFeature.name}**\n\n"
    val featureBody = this.backgroundFeature.desc.joinToString("\n\n")

    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.BACKGROUND,
        subtitle = "Character Background",
        description = featureHeading + featureBody,
        metadata = mapOf(
            "Starting Proficiencies" to proficiencies,
            "Starting Equipment" to equipment
        ),
        isHomebrew = false
    )
}

// ==========================================
// 2. CHARACTER CLASS MAPPERS
// ==========================================

/**
 * Converts a [CharacterClass] DTO to a [BrowserItem] domain model.
 */
fun CharacterClass.toDomainModel(): BrowserItem {
    val saves = this.savingThrows.joinToString { it.name }
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.CLASS,
        subtitle = "Hit Die: 1d${this.hitDie}",
        description = "A core character class focusing on specific combat or magical styles.",
        metadata = mapOf(
            "Saving Throws" to saves,
            "Proficiencies" to this.proficiencies.joinToString { it.name }
        ),
        isHomebrew = false
    )
}

/**
 * Converts a [ClassFeature] DTO to a [BrowserItem] domain model.
 */
fun ClassFeature.toDomainModel(): BrowserItem {
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.CLASS_FEATURE,
        subtitle = "Level ${this.level} ${this.classReference?.name ?: "Class"} Feature",
        description = this.desc.joinToString("\n\n"),
        isHomebrew = false
    )
}

/**
 * Converts a [Subclass] DTO to a [BrowserItem] domain model.
 */
fun Subclass.toDomainModel(): BrowserItem {
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.SUBCLASS,
        subtitle = "${this.baseClass.name} Archetype",
        description = this.desc.joinToString("\n\n"),
        isHomebrew = false
    )
}

// ==========================================
// 3. CHARACTER RACE MAPPERS
// ==========================================

/**
 * Converts a [Race] DTO to a [BrowserItem] domain model.
 */
fun Race.toDomainModel(): BrowserItem {
    val bonuses = this.abilityBonuses.joinToString { "+${it.bonus} ${it.abilityScore.name}" }
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.RACE,
        subtitle = "Base Speed: ${this.speed} ft.",
        description = "Base racial template providing early statistics and traits.",
        metadata = mapOf(
            "Ability Score Adjustments" to bonuses.ifEmpty { "None" },
            "Languages" to this.languages.joinToString { it.name }
        ),
        isHomebrew = false
    )
}

/**
 * Converts a [RacialFeature] DTO to a [BrowserItem] domain model.
 */
fun RacialFeature.toDomainModel(): BrowserItem {
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.RACIAL_FEATURE,
        subtitle = "Racial Feature",
        description = this.desc.joinToString("\n\n"),
        isHomebrew = false
    )
}

/**
 * Converts a [Subrace] DTO to a [BrowserItem] domain model.
 */
fun Subrace.toDomainModel(): BrowserItem {
    val bonuses = this.abilityBonuses?.joinToString { "+${it.bonus} ${it.abilityScore.name}" } ?: ""
    return BrowserItem(
        id = this.index,
        name = this.name,
        category = BrowseCategory.SUBRACE,
        subtitle = "Subrace of ${this.baseRace.name}",
        description = this.desc
            ?: "An ancestral variant of the core ${this.baseRace.name} lineage.",
        metadata = if (bonuses.isNotEmpty()) mapOf("Subrace Bonuses" to bonuses) else emptyMap(),
        isHomebrew = false
    )
}

// ==========================================
// 4. ITEMS & EQUIPMENT MAPPER
// ==========================================

/**
 * Converts an [Equipment] DTO to an [EquipmentItem] domain model.
 */
fun Equipment.toDomainModel(): EquipmentItem {
    val formattedCost = this.cost?.let { "${it.quantity} ${it.unit}" } ?: "0 gp"
    val formattedDamage = this.damage?.let { "${it.damageDice} ${it.damageType.name}" }

    val descStr = this.desc?.joinToString("\n\n") ?: ""
    val categoryInfo = "Category: ${this.weaponCategory ?: this.categoryRange ?: "Gear"}"
    val finalDescription = if (descStr.isNotEmpty()) "$descStr\n\n$categoryInfo" else categoryInfo

    return EquipmentItem(
        id = this.index,
        name = this.name,
        equipmentCategory = this.equipmentCategory.name,
        cost = formattedCost,
        weight = this.weight ?: 0.0,
        description = finalDescription,
        damage = formattedDamage,
        properties = this.properties?.map { it.name } ?: emptyList(),
        isHomebrew = false,
        contents = this.contents?.mapNotNull { eq ->
            (eq.equipment ?: eq.item)?.let { ref -> EquipmentContent(ref, eq.quantity) }
        } ?: emptyList()
    )
}
