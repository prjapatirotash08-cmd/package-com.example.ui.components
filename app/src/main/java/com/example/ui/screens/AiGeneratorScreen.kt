package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import com.example.domain.StudioToolId
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkspaceState

@Composable
fun AiGeneratorScreen(
    tool: StudioToolId,
    workspace: WorkspaceState,
    aiNotice: String,
    creditCost: Int,
    onGenerate: (
        prompt: String,
        negativePrompt: String,
        style: String,
        aspectRatio: String,
        quality: String,
        outputCount: Int
    ) -> Unit,
    onSaveProject: (title: String, index: Int) -> Unit,
    onExportGallery: (index: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var prompt by remember(tool) {
        mutableStateOf(
            when (tool) {
                StudioToolId.GEN_AVATAR -> "Luxury golden cyberpunk studio avatar, rim lighting"
                StudioToolId.GEN_HEADSHOT -> "Executive portrait in tailored dark blazer, warm key light"
                StudioToolId.GEN_FASHION -> "High-fashion gold-embroidered editorial lookbook portrait"
                else -> ""
            }
        )
    }
    var negativePrompt by remember { mutableStateOf("blurry, low contrast, watermark, distorted") }
    var selectedStyle by remember { mutableStateOf("Photorealistic") }
    var selectedAspect by remember { mutableStateOf("1:1") }
    var selectedQuality by remember { mutableStateOf("HD") }
    var outputCount by remember { mutableIntStateOf(2) }
    var selectedResultIdx by remember { mutableIntStateOf(0) }

    val styles = listOf("Photorealistic", "Cinematic", "3D Render", "Anime", "Editorial", "Oil Painting")
    val aspects = listOf("1:1", "16:9", "9:16", "4:3", "3:4")
    val qualities = listOf("Standard", "HD", "4K")

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
            Column {
                Text(tool.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("${creditCost} cr / output (refunded in sample preview mode)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        FirebaseNoticeBanner(notice = aiNotice)

        LuxuryCard(highlighted = true) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Describe your image prompt") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_gen_prompt_input")
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = { negativePrompt = it },
                label = { Text("Negative Prompt (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text("Style", style = MaterialTheme.typography.labelMedium, color = SecondaryGold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        label = { Text(style) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium, color = SecondaryGold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspects.forEach { ratio ->
                    FilterChip(
                        selected = selectedAspect == ratio,
                        onClick = { selectedAspect = ratio },
                        label = { Text(ratio) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Quality", style = MaterialTheme.typography.labelMedium, color = SecondaryGold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        qualities.forEach { q ->
                            FilterChip(
                                selected = selectedQuality == q,
                                onClick = { selectedQuality = q },
                                label = { Text(q) }
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Outputs: $outputCount", style = MaterialTheme.typography.labelMedium, color = SecondaryGold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..4).forEach { count ->
                            FilterChip(
                                selected = outputCount == count,
                                onClick = { outputCount = count },
                                label = { Text("$count") }
                            )
                        }
                    }
                }
            }

            if (!workspace.errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(workspace.errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(14.dp))
            GoldGradientButton(
                text = "Generate Sample Preview ($outputCount)",
                icon = Icons.Filled.AutoAwesome,
                isLoading = workspace.isProcessing,
                onClick = {
                    selectedResultIdx = 0
                    onGenerate(prompt, negativePrompt, selectedStyle, selectedAspect, selectedQuality, outputCount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_gen_submit_button")
            )
        }

        if (workspace.generatedBitmaps.isNotEmpty()) {
            SectionHeader(
                title = "Sample Preview Gallery",
                subtitle = workspace.statusSummary
            )
            val safeIdx = selectedResultIdx.coerceIn(0, workspace.generatedBitmaps.lastIndex)
            LuxuryCard(highlighted = true) {
                Image(
                    bitmap = workspace.generatedBitmaps[safeIdx].asImageBitmap(),
                    contentDescription = "Sample Preview #${safeIdx + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                if (workspace.generatedBitmaps.size > 1) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        workspace.generatedBitmaps.indices.forEach { idx ->
                            FilterChip(
                                selected = safeIdx == idx,
                                onClick = { selectedResultIdx = idx },
                                label = { Text("Preview #${idx + 1}") }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GoldGradientButton(
                        text = "Save Project",
                        icon = Icons.Filled.Save,
                        onClick = { onSaveProject("${tool.title} #${safeIdx + 1}", safeIdx) },
                        modifier = Modifier.weight(1f)
                    )
                    GoldOutlinedButton(
                        text = "Download",
                        icon = Icons.Filled.Download,
                        onClick = { onExportGallery(safeIdx) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                GoldOutlinedButton(
                    text = "Regenerate Sample",
                    icon = Icons.Filled.Refresh,
                    onClick = {
                        onGenerate(prompt, negativePrompt, selectedStyle, selectedAspect, selectedQuality, outputCount)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
