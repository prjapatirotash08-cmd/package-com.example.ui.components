package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.ExportFormat
import com.example.domain.StudioToolId
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkspaceState

@Composable
fun AiPhotoEditorScreen(
    tool: StudioToolId,
    workspace: WorkspaceState,
    aiNotice: String,
    creditCost: Int,
    onPickImageUri: (Uri) -> Unit,
    onLoadSample: (Int) -> Unit,
    onRunAiEdit: (intensity: Float, promptNote: String) -> Unit,
    onSaveProject: (title: String) -> Unit,
    onExportGallery: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var intensity by remember { mutableFloatStateOf(0.75f) }
    var promptNote by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onPickImageUri) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                }
                Column {
                    Text(tool.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("Cost: $creditCost credits (refunded in preview mode)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        FirebaseNoticeBanner(notice = aiNotice)

        // Photo Picker / Sample Selector
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            GoldOutlinedButton(
                text = "Pick Photo",
                icon = Icons.Filled.PhotoLibrary,
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_edit_pick_photo_button")
            )
            GoldOutlinedButton(
                text = "Studio Sample",
                icon = Icons.Filled.Portrait,
                onClick = { onLoadSample(R.drawable.img_showcase_portrait) },
                modifier = Modifier.weight(1f)
            )
        }

        // Image Preview or Before/After Comparison
        val src = workspace.sourceBitmap
        val out = workspace.processedBitmap
        if (src != null && out != null) {
            Text("Drag gold divider to compare Before & After", style = MaterialTheme.typography.labelSmall, color = SecondaryGold)
            BeforeAfterSlider(beforeBitmap = src, afterBitmap = out)
        } else if (src != null) {
            LuxuryCard {
                Image(
                    bitmap = src.asImageBitmap(),
                    contentDescription = "Source Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // Options Card
        LuxuryCard {
            Text("Processing Options", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
            Spacer(Modifier.height(10.dp))
            Text("Enhancement Strength: ${(intensity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = PrimaryGold)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = promptNote,
                onValueChange = { promptNote = it },
                label = {
                    Text(
                        when (tool) {
                            StudioToolId.AI_BG_CHANGER -> "Target background scene (e.g., Luxury Penthouse)"
                            StudioToolId.AI_OBJECT_REMOVER -> "Object to remove (e.g., Background passerby)"
                            StudioToolId.AI_UPSCALER -> "Target resolution (e.g., 4K Ultra HD)"
                            else -> "Optional instruction note"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (!workspace.errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(workspace.errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            if (!workspace.statusSummary.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(workspace.statusSummary, color = SecondaryGold, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(14.dp))
            GoldGradientButton(
                text = "Generate Sample Preview",
                icon = Icons.Filled.AutoFixHigh,
                isLoading = workspace.isProcessing,
                onClick = { onRunAiEdit(intensity, promptNote) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_edit_run_button")
            )
        }

        if (out != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GoldGradientButton(
                    text = "Save Project",
                    icon = Icons.Filled.Save,
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.weight(1f)
                )
                GoldOutlinedButton(
                    text = "Download",
                    icon = Icons.Filled.Download,
                    onClick = onExportGallery,
                    modifier = Modifier.weight(1f)
                )
            }
            GoldOutlinedButton(
                text = "Share Preview",
                icon = Icons.Filled.Share,
                onClick = onShare,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showSaveDialog) {
        var title by remember { mutableStateOf("${tool.title} Preview") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = CardObsidian,
            title = { Text("Save to My Projects", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProject(title)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = ObsidianBlack)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}
