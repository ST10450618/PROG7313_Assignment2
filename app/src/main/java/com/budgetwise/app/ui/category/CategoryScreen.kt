package com.budgetwise.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.ui.theme.BackgroundLight
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary
import kotlinx.coroutines.launch

/**
 * 12 curated brand-appropriate hex colour strings for the category colour picker.
 * Displayed as a 2×6 circle grid in AddCategoryDialog.
 */
val CATEGORY_COLOURS = listOf(
    "#1B998B", "#06D6A0", "#0F4C5C", "#E16162",
    "#FDD05C", "#4ECDC4", "#FF6B6B", "#A8DADC",
    "#457B9D", "#F4A261", "#2A9D8F", "#E9C46A"
)

/**
 * Parse a hex colour string to a Compose Color.
 * Handles "#RRGGBB" format with the leading #.
 * Returns TealPrimary as a safe fallback for invalid strings.
 */
fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        TealPrimary
    }
}

/**
 * Categories screen — manage expense categories.
 *
 * Layout:
 * - TopAppBar with title
 * - LazyColumn of CategoryCard items (colour circle + name + delete button)
 * - Empty state placeholder when no categories exist
 * - FAB (bottom right) → opens AddCategoryDialog
 * - AddCategoryDialog: name text field + 12-colour circle picker (2×6 grid)
 * - Delete confirmation AlertDialog
 * - Snackbar for success/error feedback
 */
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMsg.collectAsStateWithLifecycle()
    val errorMsg   by viewModel.errorMsg.collectAsStateWithLifecycle()

    var showAddDialog      by remember { mutableStateOf(false) }
    var categoryToDelete   by remember { mutableStateOf<Category?>(null) }

    val snackbarHostState  = remember { SnackbarHostState() }
    val scope              = rememberCoroutineScope()

    // Show snackbar on success or error
    LaunchedEffect(successMsg) {
        if (successMsg != null) {
            scope.launch { snackbarHostState.showSnackbar(successMsg!!) }
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            scope.launch { snackbarHostState.showSnackbar(errorMsg!!) }
            viewModel.clearMessages()
        }
    }

    // Delete confirmation dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title   = { Text("Delete Category") },
            text    = { Text("Delete '${cat.name}'? Expenses in this category will become uncategorised.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat)
                    categoryToDelete = null
                }) { Text("Delete", color = CoralAlert) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Add category dialog
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colour ->
                viewModel.addCategory(name, colour)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Categories", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick           = { showAddDialog = true },
                containerColor    = TealPrimary,
                contentColor      = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
        ) {
            if (categories.isEmpty()) {
                // Empty state
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📂", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text  = "No categories yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "Tap + to create your first category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual category row card: colour circle + name + delete icon button.
 */
@Composable
fun CategoryCard(
    category: Category,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Colour circle indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(parseColor(category.colorHex))
            )

            // Category name
            Text(
                text     = category.name,
                style    = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = "Delete ${category.name}",
                    tint               = CoralAlert
                )
            }
        }
    }
}

/**
 * Dialog for adding a new category.
 * Contains:
 * - Name OutlinedTextField
 * - 12-colour circle picker arranged in 2 rows of 6
 * - Cancel + Add buttons
 */
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var name            by remember { mutableStateOf("") }
    var selectedColour  by remember { mutableStateOf(CATEGORY_COLOURS[0]) }
    val focusManager    = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("New Category") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Name input
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Category Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        focusedLabelColor  = TealPrimary,
                        cursorColor        = TealPrimary
                    )
                )

                // Colour picker — 2 rows of 6 circles
                Text(
                    "Choose a colour",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CATEGORY_COLOURS.chunked(6).forEach { rowColours ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowColours.forEach { hex ->
                            val isSelected = hex == selectedColour
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(hex))
                                    .then(
                                        if (isSelected)
                                            Modifier.border(3.dp, Color.White, CircleShape)
                                                .border(4.dp, parseColor(hex).copy(alpha = 0.6f), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColour = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onConfirm(name, selectedColour) },
                enabled  = name.isNotBlank()
            ) {
                Text("Add", color = TealPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
