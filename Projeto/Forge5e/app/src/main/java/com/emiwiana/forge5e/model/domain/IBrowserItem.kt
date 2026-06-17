package com.emiwiana.forge5e.model.domain

/**
 * A unified interface used across the UI to display tooltips, lists, and rule descriptions.
 * Implemented by domain models that can be browsed in the SRD.
 */
interface IBrowserItem {
    /**
     * Unique identifier for the item (e.g., API index or UUID).
     */
    val id: String

    /**
     * Display name of the item.
     */
    val name: String

    /**
     * Category of the item, used for UI styling and navigation logic.
     */
    val category: BrowseCategory

    /**
     * Subtitle text, typically level/school for spells or prerequisites for feats.
     */
    val subtitle: String

    /**
     * Full description text of the item.
     */
    val description: String

    /**
     * Key-value pairs of metadata (e.g., "Casting Time" -> "1 Action").
     */
    val metadata: Map<String, String>

    /**
     * Whether the item is user-created content rather than standard SRD.
     */
    val isHomebrew: Boolean
}

/**
 * Enumeration of categories available in the browser.
 */
enum class BrowseCategory {
    SPELL, CLASS, CLASS_FEATURE, SUBCLASS, RACE, RACIAL_FEATURE, SUBRACE, FEAT, BACKGROUND, EQUIPMENT
}
