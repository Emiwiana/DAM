package com.emiwiana.forge5e.model.domain

/**
 * A concrete implementation of [IBrowserItem] used to display D&D 5e elements in the UI.
 * This model is suitable for Spells, Feats, Backgrounds, Classes, and Races.
 *
 * @property id The lookup key (e.g., API index or a custom UUID).
 * @property name The display name of the item.
 * @property category The category of the item, which dictates the UI style.
 * @property subtitle Brief information like "Level 3 Evocation" or "Prerequisite: Str 13".
 * @property description The detailed description text.
 * @property metadata Flexible key-value pairs for specific details (e.g., "Range" to "120ft").
 * @property isHomebrew True if this is user-created content, false for standard SRD.
 */
data class BrowserItem(
    override val id: String,
    override val name: String,
    override val category: BrowseCategory,
    override val subtitle: String,
    override val description: String,
    override val metadata: Map<String, String> = emptyMap(),
    override val isHomebrew: Boolean
) : IBrowserItem
