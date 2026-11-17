package com.example.ui.screens

import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.ExportFormat
import com.example.domain.StudioToolId
import com.example.domain.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkspaceState

@Composable
fun CreativeAndAdvancedEditorScreen(
    tool: StudioToolId,
    workspace: WorkspaceState,
    onPickSingleUri: (Uri) -> Unit,
    onPickCollageUris: (List<Uri>) -> Unit,
    onLoadSample: (Int) -> Unit,
    onApplyCreative: (headline: String, subtitle: String, accentColorInt: Int) -> Unit,
    onApplyAdvanced: (
        cropRatio: String,
        resizeW: Int,
        resizeH: Int,
        compressQuality: Int,
        filterPreset: String,
        effectName: String,
        effectIntensity: Float,
        overlayText: String,
        overlayTextColor: Int,
        overlayTextScale: Float,
        overlayPosition: String,
        stickerType: String,
        borderWidthPx: Int,
        borderColor: Int,
        exportFormat: ExportFormat,
        watermarkText: String,
        watermarkTiled: Boolean
    ) -> Unit,
    onSaveProject: (title: String, format: ExportFormat, quality: Int) -> Unit,
    onExportGallery: (format: ExportFormat, quality: Int) -> Unit,
    onShare: (format: ExportFormat) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    // Creative studio state
    var headline by remember(tool) {
        mutableStateOf(
            when (tool) {
                StudioToolId.STUDIO_WEDDING -> "Aarav & Meera"
                StudioToolId.STUDIO_BIRTHDAY -> "HAPPY 25TH BIRTHDAY"
                StudioToolId.STUDIO_FESTIVAL -> "DIWALI NIGHTS"
                StudioToolId.STUDIO_LOGO -> "AURA LUXE"
                StudioToolId.STUDIO_YT_THUMB -> "PRO EDITING GUIDE"
                else -> "LUXURY STUDIO"
            }
        )
    }
    var subtitle by remember(tool) {
        mutableStateOf(
            when (tool) {
                StudioToolId.STUDIO_WEDDING -> "Forever & Always • Nov 2026"
                StudioToolId.STUDIO_LOGO -> "CREATIVE STUDIO"
                else -> "Crafted in AI Photo Studio"
            }
        )
    }
    var accentColorInt by remember { mutableIntStateOf(AndroidColor.rgb(212, 175, 55)) }

    // Advanced editing state
    var cropRatio by remember { mutableStateOf("1:1") }
    var resizeWidthText by remember(workspace.sourceBitmap) {
        mutableStateOf((workspace.sourceBitmap?.width ?: 1080).toString())
    }
    var resizeHeightText by remember(workspace.sourceBitmap) {
        mutableStateOf((workspace.sourceBitmap?.height ?: 1080).toString())
    }
    var compressQuality by remember { mutableIntStateOf(72) }
    var filterPreset by remember { mutableStateOf("Golden Hour") }
    var effectName by remember { mutableStateOf("Vignette") }
    var effectIntensity by remember { mutableFloatStateOf(0.7f) }
    var overlayText by remember { mutableStateOf("AI PHOTO STUDIO") }
    var overlayTextColor by remember { mutableIntStateOf(AndroidColor.rgb(245, 215, 110)) }
    var overlayTextScale by remember { mutableFloatStateOf(1.0f) }
    var overlayPosition by remember { mutableStateOf("Bottom") }
    var stickerType by remember { mutableStateOf("Crown") }
    var borderWidthPx by remember { mutableIntStateOf(36) }
    var borderColor by remember { mutableIntStateOf(AndroidColor.rgb(212, 175, 55)) }
    var exportFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var watermarkText by remember { mutableStateOf("© AI Photo Studio") }
    var watermarkTiled by remember { mutableStateOf(true) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val singlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onPickSingleUri) }
    )
    val multiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(4),
        onResult = { uris -> if (uris.isNotEmpty()) onPickCollageUris(uris) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Real On-Device Canvas & Bitmap Engine • Free", style = MaterialTheme.typography.bodySmall, color = StatusSuccess)
            }
        }

        // Photo Picker Bar
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            GoldOutlinedButton(
                text = if (tool == StudioToolId.STUDIO_COLLAGE) "Pick Up to 4 Photos" else "Pick Photo",
                icon = Icons.Filled.PhotoLibrary,
                onClick = {
                    if (tool == StudioToolId.STUDIO_COLLAGE) {
                        multiPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        singlePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("editor_pick_photo_button")
            )
            GoldOutlinedButton(
                text = "Sample",
                icon = Icons.Filled.Portrait,
                onClick = { onLoadSample(R.drawable.img_showcase_portrait) },
                modifier = Modifier.weight(0.75f)
            )
        }

        // Canvas Preview / Before & After
        val src = workspace.sourceBitmap
        val out = workspace.processedBitmap
        if (src != null && out != null && tool.category == ToolCategory.ADVANCED_EDITING && tool != StudioToolId.ADV_CROPPER) {
            Text("Drag gold divider to compare Before & After", style = MaterialTheme.typography.labelSmall, color = SecondaryGold)
            BeforeAfterSlider(beforeBitmap = src, afterBitmap = out)
        } else {
            val displayBmp = out ?: src
            if (displayBmp != null) {
                LuxuryCard(highlighted = out != null) {
                    Image(
                        bitmap = displayBmp.asImageBitmap(),
                        contentDescription = tool.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${displayBmp.width}×${displayBmp.height}px" +
                            if (workspace.estimatedBytes > 0) " • ~${workspace.estimatedBytes / 1024}KB" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Tool-specific controls
        LuxuryCard(highlighted = true) {
            Text("Studio Controls", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
            Spacer(Modifier.height(10.dp))

            if (tool.category == ToolCategory.CREATIVE_STUDIO) {
                if (tool != StudioToolId.STUDIO_IG_DP && tool != StudioToolId.STUDIO_COLLAGE && tool != StudioToolId.STUDIO_PASSPORT) {
                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text(if (tool == StudioToolId.STUDIO_LOGO) "Brand Name" else "Headline / Names") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    if (tool != StudioToolId.STUDIO_FRAME) {
                        OutlinedTextField(
                            value = subtitle,
                            onValueChange = { subtitle = it },
                            label = { Text("Subtitle / Tagline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (tool != StudioToolId.STUDIO_PASSPORT) {
                    Text("Accent Color", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val swatches = listOf(
                            "Gold" to AndroidColor.rgb(212, 175, 55),
                            "Champagne" to AndroidColor.rgb(245, 215, 110),
                            "Ivory" to AndroidColor.rgb(245, 240, 230),
                            "Emerald" to AndroidColor.rgb(34, 197, 94)
                        )
                        swatches.forEach { (label, colorVal) ->
                            FilterChip(
                                selected = accentColorInt == colorVal,
                                onClick = { accentColorInt = colorVal },
                                label = { Text(label) }
                            )
                        }
                    }
                } else {
                    Text(
                        "Generates a standard 6-up 35×45mm passport print sheet with cut guides on crisp white paper.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                // Advanced Editing Tool Controls (31–40)
                when (tool) {
                    StudioToolId.ADV_CROPPER -> {
                        Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1:1", "4:5", "16:9", "3:4", "9:16").forEach { r ->
                                FilterChip(selected = cropRatio == r, onClick = { cropRatio = r }, label = { Text(r) })
                            }
                        }
                    }
                    StudioToolId.ADV_RESIZER -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = resizeWidthText,
                                onValueChange = { resizeWidthText = it.filter(Char::isDigit) },
                                label = { Text("Width (px)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = resizeHeightText,
                                onValueChange = { resizeHeightText = it.filter(Char::isDigit) },
                                label = { Text("Height (px)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1080×1080" to (1080 to 1080), "1280×720" to (1280 to 720), "800×600" to (800 to 600)).forEach { (lbl, dims) ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        resizeWidthText = dims.first.toString()
                                        resizeHeightText = dims.second.toString()
                                    },
                                    label = { Text(lbl) }
                                )
                            }
                        }
                    }
                    StudioToolId.ADV_COMPRESSOR -> {
                        Text("JPEG Quality: $compressQuality%", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Slider(
                            value = compressQuality.toFloat(),
                            onValueChange = { compressQuality = it.toInt() },
                            valueRange = 15f..100f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = PrimaryGold)
                        )
                    }
                    StudioToolId.ADV_FILTERS -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Golden Hour", "Noir", "Vintage", "Vivid", "Cool Midnight", "Sepia", "Matte").forEach { preset ->
                                FilterChip(
                                    selected = filterPreset == preset,
                                    onClick = { filterPreset = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                    }
                    StudioToolId.ADV_EFFECTS -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Vignette", "Warm Glow", "High Contrast", "Gold Duotone").forEach { eff ->
                                FilterChip(
                                    selected = effectName == eff,
                                    onClick = { effectName = eff },
                                    label = { Text(eff) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Effect Intensity: ${(effectIntensity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Slider(
                            value = effectIntensity,
                            onValueChange = { effectIntensity = it },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = PrimaryGold)
                        )
                    }
                    StudioToolId.ADV_TEXT -> {
                        OutlinedTextField(
                            value = overlayText,
                            onValueChange = { overlayText = it },
                            label = { Text("Overlay Text") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Text Scale: ${"%.1f".format(overlayTextScale)}x", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Slider(
                            value = overlayTextScale,
                            onValueChange = { overlayTextScale = it },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = PrimaryGold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Top", "Center", "Bottom").forEach { pos ->
                                FilterChip(selected = overlayPosition == pos, onClick = { overlayPosition = pos }, label = { Text(pos) })
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Gold" to AndroidColor.rgb(245, 215, 110),
                                "White" to AndroidColor.WHITE,
                                "Black" to AndroidColor.BLACK
                            ).forEach { (lbl, col) ->
                                FilterChip(selected = overlayTextColor == col, onClick = { overlayTextColor = col }, label = { Text(lbl) })
                            }
                        }
                    }
                    StudioToolId.ADV_STICKERS -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Crown", "Sparkle", "Star", "Heart", "Verified", "VIP").forEach { st ->
                                FilterChip(selected = stickerType == st, onClick = { stickerType = st }, label = { Text(st) })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Top", "Center", "Bottom").forEach { pos ->
                                FilterChip(selected = overlayPosition == pos, onClick = { overlayPosition = pos }, label = { Text(pos) })
                            }
                        }
                    }
                    StudioToolId.ADV_BORDER -> {
                        Text("Border Width: ${borderWidthPx}px", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Slider(
                            value = borderWidthPx.toFloat(),
                            onValueChange = { borderWidthPx = it.toInt() },
                            valueRange = 8f..120f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = PrimaryGold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Gold" to AndroidColor.rgb(212, 175, 55),
                                "Obsidian" to AndroidColor.rgb(12, 12, 12),
                                "Ivory" to AndroidColor.rgb(248, 244, 236)
                            ).forEach { (lbl, col) ->
                                FilterChip(selected = borderColor == col, onClick = { borderColor = col }, label = { Text(lbl) })
                            }
                        }
                    }
                    StudioToolId.ADV_CONVERTER -> {
                        Text("Target Format", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportFormat.entries.forEach { fmt ->
                                FilterChip(
                                    selected = exportFormat == fmt,
                                    onClick = { exportFormat = fmt },
                                    label = { Text(fmt.label) }
                                )
                            }
                        }
                    }
                    StudioToolId.ADV_WATERMARK -> {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text("Watermark Text") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tiled Diagonal Watermark", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Switch(checked = watermarkTiled, onCheckedChange = { watermarkTiled = it })
                        }
                    }
                    else -> Unit
                }
            }

            if (!workspace.statusSummary.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(workspace.statusSummary, color = StatusSuccess, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(14.dp))
            GoldGradientButton(
                text = if (tool.category == ToolCategory.CREATIVE_STUDIO) "Render Canvas Template" else "Apply Edit",
                icon = Icons.Filled.CheckCircle,
                isLoading = workspace.isProcessing,
                onClick = {
                    if (tool.category == ToolCategory.CREATIVE_STUDIO) {
                        onApplyCreative(headline, subtitle, accentColorInt)
                    } else {
                        onApplyAdvanced(
                            cropRatio,
                            resizeWidthText.toIntOrNull() ?: 1080,
                            resizeHeightText.toIntOrNull() ?: 1080,
                            compressQuality,
                            filterPreset,
                            effectName,
                            effectIntensity,
                            overlayText,
                            overlayTextColor,
                            overlayTextScale,
                            overlayPosition,
                            stickerType,
                            borderWidthPx,
                            borderColor,
                            exportFormat,
                            watermarkText,
                            watermarkTiled
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_apply_button")
            )
        }

        // Export Format & Save / Gallery / Share Actions
        LuxuryCard {
            Text("Export & Save", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportFormat.entries.forEach { fmt ->
                    FilterChip(
                        selected = exportFormat == fmt,
                        onClick = { exportFormat = fmt },
                        label = { Text(fmt.label) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GoldGradientButton(
                    text = "Save Project",
                    icon = Icons.Filled.Save,
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_save_project_button")
                )
                GoldOutlinedButton(
                    text = "Save Gallery",
                    icon = Icons.Filled.Download,
                    onClick = { onExportGallery(exportFormat, compressQuality) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_export_gallery_button")
                )
            }
            Spacer(Modifier.height(8.dp))
            GoldOutlinedButton(
                text = "Share Image",
                icon = Icons.Filled.Share,
                onClick = { onShare(exportFormat) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showSaveDialog) {
        var title by remember { mutableStateOf("${tool.title} Edit") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = CardObsidian,
            title = { Text("Save Project", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProject(title, exportFormat, compressQuality)
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
