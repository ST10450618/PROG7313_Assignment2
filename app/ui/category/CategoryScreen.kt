package com.budgetwise.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.data.local.entity.Category
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary

val CATEGORY_COLOURS = listOf(
    "#1B998B","#06D6A0","#0F4C5C","#E16162",
    "#FDD05C","#3A86FF","#FF6B6B","#6BCB77",
    "#845EC2","#FF9671","#4B4453","#00C9A7"
)

fun parseColour(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color(0xFF1B998B) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(vm: CategoryViewModel = hiltViewModel()) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    val ui         by vm.uiState.collectAsStateWithLifecycle()
    val snack       = remember { SnackbarHostState() }
    var showAdd    by remember { mutableStateOf(false) }
    var toDelete   by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(ui.success, ui.error) {
        (ui.success ?: ui.error)?.let { snack.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title  = { Text("Categories", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = TealPrimary) {
                Icon(Icons.Filled.Add, "Add Category", tint = Color.White)
            }
        }
    ) { pad ->
        if (categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Category, null, Modifier.size(64.dp), tint = TealPrimary.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                    Text("No categories yet", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to create one", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pad),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(parseColour(cat.colorHex)))
                            Spacer(Modifier.width(16.dp))
                            Text(cat.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { toDelete = cat }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = CoralAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddCategoryDialog(onDismiss = { showAdd = false }, onConfirm = { n, c -> vm.add(n, c); showAdd = false })

    toDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete Category") },
            text  = { Text("Delete '${cat.name}'? Associated expenses will become uncategorised.") },
            confirmButton = { TextButton(onClick = { vm.delete(cat); toDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = CoralAlert)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name     by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(CATEGORY_COLOURS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text  = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text("Pick a colour", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                CATEGORY_COLOURS.chunked(6).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        row.forEach { hex ->
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(parseColour(hex))
                                    .then(if (selected == hex) Modifier.border(3.dp, Color.Black, CircleShape) else Modifier)
                                    .clickable { selected = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onConfirm(name, selected) }, enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}