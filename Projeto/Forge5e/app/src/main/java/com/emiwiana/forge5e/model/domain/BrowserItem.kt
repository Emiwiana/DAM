package com.emiwiana.forge5e.model.domain

/**
 * A unified model used across the UI to display tooltips, lists, and rule descriptions.
 * Works perfectly for Spells, Feats, Backgrounds, Classes, and Races.
 */
data class BrowserItem(
    override val id: String,                 // The lookup key (e.g., "fireball" or a custom UUID)
    override val name: String,
    override val category: BrowseCategory,   // Dictates what icon or layout style the UI uses
    override val subtitle: String,           // e.g., "Level 3 Evocation", "Prerequisite: Str 13"
    override val description: String,        // The clean, combined text blocks for the tooltip
    override val metadata: Map<String, String> = emptyMap(), // Flexible key-value pairs (e.g., "Range" to "120ft")
    override val isHomebrew: Boolean         // Set to false for SRD, true for future manual creations
) : IBrowserItem
