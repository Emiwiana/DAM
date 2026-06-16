package com.emiwiana.forge5e.model.domain

/**
 * A unified model used across the UI to display tooltips, lists, and rule descriptions.
 * Implemented by domain models like Spells, Feats, Backgrounds, Classes, and Equipment.
 */
interface IBrowserItem {
    val id: String
    val name: String
    val category: BrowseCategory
    val subtitle: String
    val description: String
    val metadata: Map<String, String>
    val isHomebrew: Boolean
}

enum class BrowseCategory {
    SPELL, CLASS, CLASS_FEATURE, SUBCLASS, RACE, RACIAL_FEATURE, SUBRACE, FEAT, BACKGROUND, EQUIPMENT
}