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
import com.emiwiana.forge5e.model.domain.BrowseCategory
import com.emiwiana.forge5e.ui.components.FeatureCard
import com.emiwiana.forge5e.viewModel.*

@Composable
fun SrdBrowserScreen(viewModel: SrdBrowserViewModel) {
    val categoryState by viewModel.categoryState.collectAsState()
    val cardState by viewModel.cardState.collectAsState()
    val subItemsState by viewModel.subItemsState.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    var menuOpen by remember { mutableStateOf(true) }

    if (isPortrait) {
        // Handle system back button: Close menu first, then navigate history
        BackHandler(enabled = menuOpen || canGoBack) {
            if (menuOpen) {
                menuOpen = false
            } else if (canGoBack) {
                viewModel.navigateBack()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // BACKGROUND: Feature Card Detail and Sub-items
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (cardState) {
                    is CardUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is CardUiState.ShowBrowserItem -> {
                        val browserItem = (cardState as CardUiState.ShowBrowserItem).item
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            FeatureCard(item = browserItem)

                            // Subraces/Subclasses and Core features under the FeatureCard
                            if (browserItem.category == BrowseCategory.CLASS || browserItem.category == BrowseCategory.RACE) {
                                Spacer(modifier = Modifier.height(16.dp))
                                SubItemsSection(
                                    subItemsState = subItemsState,
                                    onSubItemClick = { index, isVariant ->
                                        val targetCategory = when (browserItem.category) {
                                            BrowseCategory.CLASS -> if (isVariant) BrowseCategory.SUBCLASS else BrowseCategory.CLASS_FEATURE
                                            BrowseCategory.RACE -> if (isVariant) BrowseCategory.SUBRACE else BrowseCategory.RACIAL_FEATURE
                                            else -> BrowseCategory.CLASS
                                        }
                                        viewModel.loadFeatureCard(index, targetCategory)
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select an item from the menu to view details.")
                        }
                    }
                }
            }

            // FLOATING ACTION BUTTONS (when menu is closed)
            if (!menuOpen) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canGoBack) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.navigateBack() },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                        }
                    }
                    SmallFloatingActionButton(
                        onClick = { menuOpen = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                    }
                }
            }

            // OVERLAY MENU: MasterList content
            AnimatedVisibility(
                visible = menuOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                BackHandler { menuOpen = false }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryTabs(
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { viewModel.selectCategory(it) }
                                )
                            }
                            IconButton(onClick = { menuOpen = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Menu")
                            }
                        }

                        HorizontalDivider()

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // Master List Items
                            when (categoryState) {
                                is CategoryUiState.Loading -> item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is CategoryUiState.Error -> item {
                                    Text("Error loading list", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                                }
                                is CategoryUiState.Success -> {
                                    val items = (categoryState as CategoryUiState.Success).list
                                    items(items) { item ->
                                        ListItem(
                                            headlineContent = { Text(item.name) },
                                            modifier = Modifier.clickable {
                                                viewModel.loadFeatureCard(item.index)
                                                menuOpen = false
                                            }
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    } else {
        // LANDSCAPE: Split design
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT COLUMN: Master List
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CategoryTabs(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )

                HorizontalDivider()

                when (categoryState) {
                    is CategoryUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is CategoryUiState.Error -> Text("Error loading list", color = MaterialTheme.colorScheme.error)
                    is CategoryUiState.Success -> {
                        val items = (categoryState as CategoryUiState.Success).list
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items) { item ->
                                ListItem(
                                    headlineContent = { Text(item.name) },
                                    modifier = Modifier.clickable { viewModel.loadFeatureCard(item.index) }
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            VerticalDivider()

            // RIGHT COLUMN: Feature Card Detail & Contextual Nested Items
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                if (canGoBack) {
                    TextButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go Back")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (cardState) {
                        is CardUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is CardUiState.ShowBrowserItem -> {
                            val browserItem = (cardState as CardUiState.ShowBrowserItem).item
                            FeatureCard(item = browserItem)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contextual Drill-Down Lists for Classes/Races
                            if (browserItem.category == BrowseCategory.CLASS || browserItem.category == BrowseCategory.RACE) {
                                SubItemsSection(
                                    subItemsState = subItemsState,
                                    onSubItemClick = { index, isVariant ->
                                        val targetCategory = when (browserItem.category) {
                                            BrowseCategory.CLASS -> if (isVariant) BrowseCategory.SUBCLASS else BrowseCategory.CLASS_FEATURE
                                            BrowseCategory.RACE -> if (isVariant) BrowseCategory.SUBRACE else BrowseCategory.RACIAL_FEATURE
                                            else -> BrowseCategory.CLASS
                                        }
                                        viewModel.loadFeatureCard(index, targetCategory)
                                    }
                                )
                            }
                        }
                        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Select an item from the list to view details.") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubItemsSection(
    subItemsState: SubItemsUiState,
    onSubItemClick: (String, Boolean) -> Unit
) {
    when (subItemsState) {
        is SubItemsUiState.Loading -> CircularProgressIndicator()
        is SubItemsUiState.Success -> {
            Column {
                if (subItemsState.variants.isNotEmpty()) {
                    Text("Variants & Archetypes", fontWeight = FontWeight.Bold)
                    subItemsState.variants.forEach { variant ->
                        Text(
                            text = "• ${variant.name}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onSubItemClick(variant.index, true) }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (subItemsState.features.isNotEmpty()) {
                    Text("Core Features", fontWeight = FontWeight.Bold)
                    subItemsState.features.forEach { feature ->
                        Text(
                            text = "• ${feature.name}",
                            modifier = Modifier
                                .clickable { onSubItemClick(feature.index, false) }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: BrowseCategory,
    onCategorySelected: (BrowseCategory) -> Unit
) {
    val categories = listOf(
        BrowseCategory.CLASS to "Classes",
        BrowseCategory.RACE to "Races",
        BrowseCategory.SPELL to "Spells",
        BrowseCategory.FEAT to "Feats",
        BrowseCategory.BACKGROUND to "Backgrounds",
        BrowseCategory.EQUIPMENT to "Equipment"
    )
    val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    ScrollableTabRow(selectedTabIndex = selectedIndex) {
        categories.forEach { (category, label) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(label) }
            )
        }
    }
}
