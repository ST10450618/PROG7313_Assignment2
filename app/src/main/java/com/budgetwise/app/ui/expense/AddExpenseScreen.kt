package com.budgetwise.app.ui.expense

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budgetwise.app.ui.theme.BackgroundLight
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.YellowHighlight
import com.budgetwise.app.utils.DateUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// =============================================================================
// Helper functions
// =============================================================================

/**
 * Create a unique temporary image file for storing a receipt photo.
 *
 * File: receipt_YYYYMMDD_HHmmss_SSS.jpg
 * Location: Context.getExternalFilesDir("photos") — app-private, no permission needed API 29+
 * Stored in state before launching camera so TakePicture callback can access it.
 *
 * @return The created File object.
 */
fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir("photos")
        ?: context.filesDir  // fallback to internal if external unavailable
    storageDir.mkdirs()
    return File.createTempFile("receipt_${timestamp}_", ".jpg", storageDir)
}

/**
 * Calculate the duration between start and end times in "X hr Y min" format.
 * Both times are "HH:mm" strings.
 *
 * @return A human-readable duration string, or "" if times are invalid.
 */
fun calculateDuration(startTime: String, endTime: String): String {
    return try {
        val (sh, sm) = startTime.split(":").map { it.toInt() }
        val (eh, em) = endTime.split(":").map { it.toInt() }
        val totalMinutes = (eh * 60 + em) - (sh * 60 + sm)
        if (totalMinutes <= 0) return ""
        val hours   = totalMinutes / 60
        val minutes = totalMinutes % 60
        when {
            hours == 0   -> "$minutes min"
            minutes == 0 -> "$hours hr"
            else         -> "$hours hr $minutes min"
        }
    } catch (e: Exception) { "" }
}

// =============================================================================
// BudgetWiseTimePickerDialog
// =============================================================================

/**
 * Custom dialog wrapper for Material3 TimePicker.
 *
 * Material3 BOM 2024.05.00 provides TimePicker state and composable but no
 * dialog wrapper. This composable adds OK + Cancel buttons in an AlertDialog shell.
 *
 * Uses is24Hour=true for unambiguous time entry (no AM/PM confusion).
 * Initial hour/minute are set from Calendar.getInstance() (current time).
 *
 * @param onTimeSelected  Callback with the selected "HH:mm" string on OK.
 * @param onDismiss       Called when the user taps Cancel or outside the dialog.
 * @param initialHour     Pre-selected hour (0–23). Defaults to current hour.
 * @param initialMinute   Pre-selected minute (0–59). Defaults to current minute.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetWiseTimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss:      () -> Unit,
    initialHour:    Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute:  Int = Calendar.getInstance().get(Calendar.MINUTE)
) {
    val timePickerState = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text("Select Time") },
        text   = {
            TimePicker(
                state  = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val formatted = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                onTimeSelected(formatted)
            }) { Text("OK", color = TealPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// =============================================================================
// AddExpenseScreen
// =============================================================================

/**
 * Add Expense Screen — full expense entry form.
 *
 * RUBRIC-MANDATORY FIELDS (all 7 required):
 * 1. Amount     — decimal text field with regex validation
 * 2. Description — text field (max 100 chars with counter)
 * 3. Date       — DatePickerDialog (Material3), stored as startOfDay(selectedMs)
 * 4. Start Time — BudgetWiseTimePickerDialog, stored as "HH:mm" 24-hour
 * 5. End Time   — BudgetWiseTimePickerDialog, must be after start time
 * 6. Category   — ExposedDropdownMenuBox from Room categories flow
 * 7. Photo      — Optional. Camera permission → createImageFile → FileProvider → TakePicture → AsyncImage thumbnail
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel:      ExpenseViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context    = LocalContext.current

    // Navigate back on successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    // ---- Form state ----
    var amountStr       by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var selectedDateMs  by remember { mutableStateOf(System.currentTimeMillis()) }
    var startTime       by remember { mutableStateOf("") }
    var endTime         by remember { mutableStateOf("") }
    var selectedCatId   by remember { mutableStateOf<Long?>(null) }
    var selectedCatName by remember { mutableStateOf("") }
    var capturedUri     by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoFile   by remember { mutableStateOf<File?>(null) }

    // ---- Dialog visibility ----
    var showDatePicker       by remember { mutableStateOf(false) }
    var showStartTimePicker  by remember { mutableStateOf(false) }
    var showEndTimePicker    by remember { mutableStateOf(false) }
    var categoryExpanded     by remember { mutableStateOf(false) }

    // ---- Camera permission + launcher ----
    var launchCameraAfterPermission by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            capturedUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempPhotoFile!!
            )
        } else {
            tempPhotoFile?.delete()
            capturedUri = null
        }
        launchCameraAfterPermission = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraAfterPermission = true
        } else {
            Toast.makeText(context, "Camera permission is required to capture receipts", Toast.LENGTH_LONG).show()
        }
    }

    // Launch camera once permission is granted (LaunchedEffect avoids calling in callback)
    LaunchedEffect(launchCameraAfterPermission) {
        if (launchCameraAfterPermission) {
            tempPhotoFile = createImageFile(context)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempPhotoFile!!
            )
            cameraLauncher.launch(contentUri)
        }
    }

    // ---- Date picker dialog ----
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMs
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMs = it }
                    showDatePicker = false
                }) { Text("OK", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ---- Time picker dialogs ----
    if (showStartTimePicker) {
        BudgetWiseTimePickerDialog(
            onTimeSelected = { time -> startTime = time; showStartTimePicker = false },
            onDismiss      = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        BudgetWiseTimePickerDialog(
            onTimeSelected = { time -> endTime = time; showEndTimePicker = false },
            onDismiss      = { showEndTimePicker = false }
        )
    }

    // ---- UI ----
    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Add Expense", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---- Amount ----
            OutlinedTextField(
                value         = amountStr,
                onValueChange = { v ->
                    // Allow only valid decimal: up to 8 digits, optional decimal point with 2 places
                    if (v.isEmpty() || v.matches(Regex("^\\d{0,8}(\\.\\d{0,2})?$"))) {
                        amountStr = v
                    }
                    viewModel.clearMessages()
                },
                label         = { Text("Amount (R)") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor  = TealPrimary,
                    cursorColor        = TealPrimary
                )
            )

            // ---- Description ----
            OutlinedTextField(
                value         = description,
                onValueChange = { if (it.length <= 100) description = it; viewModel.clearMessages() },
                label         = { Text("Description") },
                singleLine    = false,
                maxLines      = 3,
                modifier      = Modifier.fillMaxWidth(),
                supportingText = { Text("${description.length}/100") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor  = TealPrimary,
                    cursorColor        = TealPrimary
                )
            )

            // ---- Date picker ----
            OutlinedTextField(
                value         = DateUtils.formatDate(selectedDateMs),
                onValueChange = {},
                label         = { Text("Date") },
                readOnly      = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .then(Modifier),
                trailingIcon  = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Change", color = TealPrimary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor    = TealPrimary
                )
            )

            // ---- Start Time ----
            OutlinedTextField(
                value         = startTime.ifBlank { "Tap to select" },
                onValueChange = {},
                label         = { Text("Start Time") },
                readOnly      = true,
                modifier      = Modifier.fillMaxWidth(),
                trailingIcon  = {
                    TextButton(onClick = { showStartTimePicker = true }) {
                        Text("Pick", color = TealPrimary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor    = TealPrimary
                )
            )

            // ---- End Time ----
            OutlinedTextField(
                value         = endTime.ifBlank { "Tap to select" },
                onValueChange = {},
                label         = { Text("End Time") },
                readOnly      = true,
                modifier      = Modifier.fillMaxWidth(),
                trailingIcon  = {
                    TextButton(onClick = { showEndTimePicker = true }) {
                        Text("Pick", color = TealPrimary)
                    }
                },
                supportingText = {
                    val duration = if (startTime.isNotBlank() && endTime.isNotBlank())
                        calculateDuration(startTime, endTime) else ""
                    if (duration.isNotBlank()) Text("Duration: $duration", color = TealPrimary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor    = TealPrimary
                )
            )

            // ---- Category dropdown ----
            ExposedDropdownMenuBox(
                expanded        = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value         = selectedCatName.ifBlank { "Select category" },
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Category") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        focusedLabelColor  = TealPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded        = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text    = { Text("No categories — create one first") },
                            onClick = { categoryExpanded = false }
                        )
                    } else {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.name) },
                                onClick = {
                                    selectedCatId   = cat.id
                                    selectedCatName = cat.name
                                    categoryExpanded = false
                                    viewModel.clearMessages()
                                }
                            )
                        }
                    }
                }
            }

            // ---- Camera capture section ----
            Card(
                modifier  = Modifier.fillMaxWidth(),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text  = "Receipt Photo (Optional)",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (capturedUri != null) {
                        // Show thumbnail of captured photo
                        AsyncImage(
                            model             = capturedUri,
                            contentDescription = "Receipt photo",
                            modifier          = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale      = ContentScale.Crop
                        )
                        Row(
                            verticalAlignment  = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = TealPrimary
                            )
                            Text("Photo captured", color = TealPrimary, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { capturedUri = null; tempPhotoFile?.delete() }) {
                                Text("Remove", color = CoralAlert)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint   = YellowHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text  = if (capturedUri != null) "Retake Photo" else "Capture Receipt Photo",
                            color = TealPrimary
                        )
                    }
                }
            }

            // ---- Error message ----
            if (uiState.errorMsg != null) {
                Text(
                    text  = uiState.errorMsg!!,
                    color = CoralAlert,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---- Save button ----
            Button(
                onClick = {
                    viewModel.saveExpense(
                        amountStr   = amountStr,
                        description = description,
                        dateMs      = selectedDateMs,
                        startTime   = startTime,
                        endTime     = endTime,
                        categoryId  = selectedCatId,
                        photoUri    = capturedUri?.toString()
                    )
                },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Expense", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
