package com.emiwiana.forge5e.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emiwiana.forge5e.model.api.dto.APIReference
import com.emiwiana.forge5e.model.api.dto.character.characterClass.EquipmentOption
import com.emiwiana.forge5e.model.domain.BrowseCategory
import com.emiwiana.forge5e.ui.components.FeatureCard
import com.emiwiana.forge5e.viewModel.*

/**
 * Main screen for browsing SRD content.
 * Supports both portrait and landscape layouts with a responsive design.
 *
 * @param viewModel The [SrdBrowserViewModel] providing the state and logic.
 */
@Composable
fun SrdBrowserScreen(viewModel: SrdBrowserViewModel) {
    val categoryState by viewModel.categoryState.collectAsState()
    val cardState by viewModel.cardState.collectAsState()
    val subItemsState by viewModel.subItemsState.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    var menuOpen by remember { mutableStateOf(true) }

    val onCategoryItemClick: (APIReference) -> Unit = { item ->
        viewModel.loadFeatureCard(item.index)
        if (isPortrait) menuOpen = false
    }

    if (isPortrait) {
        PortraitLayout(
            menuOpen = menuOpen,
            canGoBack = canGoBack,
            categoryState = categoryState,
            cardState = cardState,
            subItemsState = subItemsState,
            selectedCategory = selectedCategory,
            onCloseMenu = { menuOpen = false },
            onOpenMenu = { menuOpen = true },
            onNavigateBack = { viewModel.navigateBack() },
            onCategorySelected = { viewModel.selectCategory(it) },
            onItemClick = onCategoryItemClick,
            onSubItemClick = { index, category -> viewModel.loadFeatureCard(index, category) }
        )
    } else {
        LandscapeLayout(
            canGoBack = canGoBack,
            categoryState = categoryState,
            cardState = cardState,
            subItemsState = subItemsState,
            selectedCategory = selectedCategory,
            onNavigateBack = { viewModel.navigateBack() },
            onCategorySelected = { viewModel.selectCategory(it) },
            onItemClick = onCategoryItemClick,
            onSubItemClick = { index, category -> viewModel.loadFeatureCard(index, category) }
        )
    }
}

/**
 * Portrait-specific layout for the SRD Browser.
 * Features an animated side menu for category and item selection.
 */
@Composable
private fun PortraitLayout(
    menuOpen: Boolean,
    canGoBack: Boolean,
    categoryState: CategoryUiState,
    cardState: CardUiState,
    subItemsState: SubItemsUiState,
    selectedCategory: BrowseCategory,
    onCloseMenu: () -> Unit,
    onOpenMenu: () -> Unit,
    onNavigateBack: () -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onItemClick: (APIReference) -> Unit,
    onSubItemClick: (String, BrowseCategory) -> Unit
) {
    BackHandler(enabled = menuOpen || canGoBack) {
        if (menuOpen) onCloseMenu() else onNavigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DetailContent(
            cardState = cardState,
            subItemsState = subItemsState,
            onSubItemClick = onSubItemClick,
            modifier = Modifier.padding(16.dp)
        )

        if (!menuOpen) {
            FloatingActionButtons(
                canGoBack = canGoBack,
                onNavigateBack = onNavigateBack,
                onOpenMenu = onOpenMenu,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Surface(modifier = Modifier.fillMaxSize(), tonalElevation = 4.dp) {
                CategoryMenu(
                    selectedCategory = selectedCategory,
                    categoryState = categoryState,
                    onCategorySelected = onCategorySelected,
                    onItemClick = onItemClick,
                    onCloseMenu = onCloseMenu
                )
            }
        }
    }
}

/**
 * Landscape-specific layout for the SRD Browser.
 * Displays the list and details side-by-side.
 */
@Composable
private fun LandscapeLayout(
    canGoBack: Boolean,
    categoryState: CategoryUiState,
    cardState: CardUiState,
    subItemsState: SubItemsUiState,
    selectedCategory: BrowseCategory,
    onNavigateBack: () -> Unit,
    onCategorySelected: (BrowseCategory) -> Unit,
    onItemClick: (APIReference) -> Unit,
    onSubItemClick: (String, BrowseCategory) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            CategoryTabs(selectedCategory, onCategorySelected)
            HorizontalDivider()
            CategoryList(categoryState, onItemClick)
        }

        VerticalDivider()

        Column(modifier = Modifier.weight(1.5f).fillMaxHeight().padding(16.dp)) {
            if (canGoBack) {
                TextButton(onClick = onNavigateBack, modifier = Modifier.padding(bottom = 8.dp).align(Alignment.End)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go Back")
                }
            }
            DetailContent(cardState, subItemsState, onSubItemClick)
        }
    }
}

/**
 * Displays the detailed content of the selected item.
 */
@Composable
private fun DetailContent(
    cardState: CardUiState,
    subItemsState: SubItemsUiState,
    onSubItemClick: (String, BrowseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (cardState) {
            is CardUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is CardUiState.ShowBrowserItem -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    FeatureCard(item = cardState.item)
                    Spacer(modifier = Modifier.height(16.dp))
                    SubItemsSection(subItemsState, cardState.item.category, onSubItemClick)
                }
            }
            is CardUiState.Error -> Text(cardState.message, modifier = Modifier.align(Alignment.Center))
            else -> Text("Select an item to view details.", modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Sidebar menu content for portrait layout.
 */
@Composable
private fun CategoryMenu(
    selectedCategory: BrowseCategory,
    categoryState: CategoryUiState,
    onCategorySelected: (BrowseCategory) -> Unit,
    onItemClick: (APIReference) -> Unit,
    onCloseMenu: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CategoryTabs(selectedCategory, onCategorySelected)
            }
            IconButton(onClick = onCloseMenu) {
                Icon(Icons.Default.Close, contentDescription = "Close Menu")
            }
        }
        HorizontalDivider()
        CategoryList(categoryState, onItemClick)
    }
}

/**
 * Scrollable list of items within a category.
 */
@Composable
private fun CategoryList(state: CategoryUiState, onItemClick: (APIReference) -> Unit) {
    when (state) {
        is CategoryUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is CategoryUiState.Success -> {
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.list) { item ->
                    ListItem(headlineContent = { Text(item.name) }, modifier = Modifier.clickable { onItemClick(item) })
                }
            }
        }
        is CategoryUiState.Error -> Text(state.message, modifier = Modifier.padding(16.dp))
        else -> {}
    }
}

/**
 * Floating action buttons for navigation in portrait mode.
 */
@Composable
private fun FloatingActionButtons(
    canGoBack: Boolean,
    onNavigateBack: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canGoBack) {
            SmallFloatingActionButton(onClick = onNavigateBack, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
            }
        }
        SmallFloatingActionButton(onClick = onOpenMenu, containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
        }
    }
}

/**
 * Section displaying related items like features, variants, or equipment.
 */
@Composable
private fun SubItemsSection(
    subItemsState: SubItemsUiState,
    currentCategory: BrowseCategory,
    onSubItemClick: (String, BrowseCategory) -> Unit
) {
    when (subItemsState) {
        is SubItemsUiState.Loading -> CircularProgressIndicator()
        is SubItemsUiState.Success -> {
            Column {
                SubItemCategory("Variants & Archetypes", subItemsState.variants) { 
                    val target = if (currentCategory == BrowseCategory.RACE) BrowseCategory.SUBRACE else BrowseCategory.SUBCLASS
                    onSubItemClick(it.index, target) 
                }

                val featureLabel = when (currentCategory) {
                    BrowseCategory.RACE, BrowseCategory.SUBRACE -> "Racial Traits"
                    BrowseCategory.SUBCLASS -> "Subclass Features"
                    else -> "Core Features"
                }
                SubItemCategory(featureLabel, subItemsState.features) {
                    val target = if (currentCategory == BrowseCategory.RACE || currentCategory == BrowseCategory.SUBRACE) BrowseCategory.RACIAL_FEATURE else BrowseCategory.CLASS_FEATURE
                    onSubItemClick(it.index, target)
                }

                if (subItemsState.equipment.isNotEmpty() || subItemsState.equipmentChoices.isNotEmpty()) {
                    val label = if (currentCategory == BrowseCategory.EQUIPMENT) "Contents" else "Starting Equipment"
                    Text(label, fontWeight = FontWeight.Bold)
                    subItemsState.equipment.forEach { eq ->
                        eq.equipment?.let { ref ->
                            Text(
                                text = "• ${if (eq.quantity > 1) "${eq.quantity}x " else ""}${ref.name}",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onSubItemClick(ref.index, BrowseCategory.EQUIPMENT) }.padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }
                    subItemsState.equipmentChoices.forEach { choice ->
                        choice.desc?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp)) }
                        choice.from.options?.forEach { EquipmentOptionItem(it, onSubItemClick) }
                        choice.from.equipmentCategory?.let { Text("  - Any ${it.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp)) }
                    }
                }
            }
        }
        else -> {}
    }
}

/**
 * Helper component for displaying a list of sub-items under a label.
 */
@Composable
private fun SubItemCategory(label: String, items: List<APIReference>, onClick: (APIReference) -> Unit) {
    if (items.isNotEmpty()) {
        Text(label, fontWeight = FontWeight.Bold)
        items.forEach { item ->
            Text(
                text = "• ${item.name}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onClick(item) }.padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Recursive component for displaying nested equipment options.
 */
@Composable
private fun EquipmentOptionItem(option: EquipmentOption, onSubItemClick: (String, BrowseCategory) -> Unit, indent: Int = 16) {
    val itemRef = option.item ?: option.of
    itemRef?.let {
        Text(
            text = "  - ${option.count ?: ""} ${it.name}".trim(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onSubItemClick(it.index, BrowseCategory.EQUIPMENT) }.padding(vertical = 2.dp, horizontal = indent.dp)
        )
    }
    option.items?.forEach { EquipmentOptionItem(it, onSubItemClick, indent + 8) }
    option.choice?.let { choice ->
        choice.desc?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, start = indent.dp)) }
        choice.from.options?.forEach { EquipmentOptionItem(it, onSubItemClick, indent + 8) }
    }
}

/**
 * Scrollable tabs for selecting the browsing category.
 */
@Composable
private fun CategoryTabs(selectedCategory: BrowseCategory, onCategorySelected: (BrowseCategory) -> Unit) {
    val categories = remember {
        listOf(
            BrowseCategory.CLASS to "Classes",
            BrowseCategory.RACE to "Races",
            BrowseCategory.SPELL to "Spells",
            BrowseCategory.FEAT to "Feats",
            BrowseCategory.BACKGROUND to "Backgrounds",
            BrowseCategory.EQUIPMENT to "Equipment"
        )
    }
    val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    ScrollableTabRow(selectedTabIndex = selectedIndex) {
        categories.forEach { (category, label) ->
            Tab(selected = selectedCategory == category, onClick = { onCategorySelected(category) }, text = { Text(label) })
        }
    }
}
