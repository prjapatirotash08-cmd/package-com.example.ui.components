package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SubscriptionPlanEntity
import com.example.data.processing.ImageProcessingEngine
import com.example.domain.StudioToolId
import com.example.domain.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    plans: List<SubscriptionPlanEntity>,
    aiNotice: String,
    onStartCreating: () -> Unit,
    onExploreTools: () -> Unit,
    onAdminLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showcaseBefore by remember { mutableStateOf<Bitmap?>(null) }
    var showcaseAfter by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val engine = ImageProcessingEngine(context)
        val raw = BitmapFactory.decodeResource(context.resources, R.drawable.img_showcase_portrait)
        if (raw != null) {
            showcaseBefore = raw
            val processed = engine.processAdvancedTool(
                tool = StudioToolId.ADV_FILTERS,
                source = raw,
                filterPreset = "Golden Hour"
            )
            showcaseAfter = processed.bitmap
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // Top Brand Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldGradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = "AI Photo Studio",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "AI PHOTO STUDIO",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SecondaryGold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onAdminLogin,
                        modifier = Modifier.testTag("landing_admin_button")
                    ) {
                        Text("Admin", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF231C0E),
                        border = BorderStroke(1.dp, PrimaryGold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onStartCreating)
                            .testTag("landing_signin_button")
                    ) {
                        Text(
                            "Sign In",
                            color = SecondaryGold,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 1. Premium Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardObsidian)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_studio),
                    contentDescription = "AI Photo Studio Luxury Workspace",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBlack.copy(alpha = 0.25f),
                                    ObsidianBlack.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Surface(
                        color = Color(0xFF231C0E),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, PrimaryGold)
                    ) {
                        Text(
                            "LUXURY CREATIVE SAAS • 60 FEATURES",
                            color = SecondaryGold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Transform Your Photos with AI Magic",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Create, Edit, Enhance and Generate Stunning Images with AI Photo Studio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GoldGradientButton(
                            text = "Start Creating",
                            icon = Icons.Filled.AutoAwesome,
                            onClick = onStartCreating,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_start_creating_button")
                        )
                        GoldOutlinedButton(
                            text = "Explore AI Tools",
                            onClick = onExploreTools,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_explore_tools_button")
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 2. AI Photo Studio Introduction + Honest Firebase AI Notice
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FirebaseNoticeBanner(notice = aiNotice)
                Spacer(Modifier.height(20.dp))
                SectionHeader(
                    title = "Four Specialized Studios",
                    subtitle = "40 dedicated editing & generation tools plus complete User & Admin suites"
                )
                Spacer(Modifier.height(12.dp))
                ToolCategory.entries.forEach { category ->
                    LuxuryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        onClick = onStartCreating
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF231C0E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (category) {
                                        ToolCategory.AI_EDITING -> Icons.Filled.AutoFixHigh
                                        ToolCategory.AI_GENERATOR -> Icons.Filled.AutoAwesome
                                        ToolCategory.CREATIVE_STUDIO -> Icons.Filled.Dashboard
                                        ToolCategory.ADVANCED_EDITING -> Icons.Filled.Tune
                                    },
                                    contentDescription = category.title,
                                    tint = PrimaryGold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(category.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Surface(
                                color = if (category.requiresFirebaseAi) Color(0xFF2B210E) else Color(0xFF102819),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (category.requiresFirebaseAi) "Preview" else "Live",
                                    color = if (category.requiresFirebaseAi) SecondaryGold else StatusSuccess,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Popular AI Tools & 4. AI Image Generator Preview
        item {
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(
                    title = "Popular Studio Tools",
                    subtitle = "Instant access to professional presets & templates"
                )
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val featured = listOf(
                    StudioToolId.ADV_FILTERS,
                    StudioToolId.STUDIO_IG_DP,
                    StudioToolId.STUDIO_YT_THUMB,
                    StudioToolId.ADV_COMPRESSOR,
                    StudioToolId.AI_ENHANCER,
                    StudioToolId.GEN_TEXT_TO_IMAGE
                )
                items(featured) { tool ->
                    LuxuryCard(
                        modifier = Modifier.width(180.dp),
                        onClick = onStartCreating
                    ) {
                        Icon(
                            imageVector = iconForTool(tool),
                            contentDescription = tool.title,
                            tint = PrimaryGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(tool.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
                        Spacer(Modifier.height(4.dp))
                        Text(tool.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                    }
                }
            }
        }

        // 5. Creative Studio Showcase
        item {
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(
                    title = "Creative Design Studio",
                    subtitle = "Posters, Thumbnails, Logos, Collages & Passport Sheets"
                )
                Spacer(Modifier.height(12.dp))
                LuxuryCard(highlighted = true, onClick = onStartCreating) {
                    Image(
                        painter = painterResource(id = R.drawable.img_showcase_creative),
                        contentDescription = "Creative Studio Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Real Canvas Engine • Instant PNG / JPG / WEBP Export",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryGold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Design social banners, wedding monograms, monogram logos, and 6-up passport print sheets right on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 6. Before and After Showcase
        item {
            val before = showcaseBefore
            val after = showcaseAfter
            if (before != null && after != null) {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = "Before & After Comparison",
                        subtitle = "Drag the gold divider to compare Golden Hour studio grading"
                    )
                    Spacer(Modifier.height(12.dp))
                    BeforeAfterSlider(beforeBitmap = before, afterBitmap = after)
                }
            }
        }

        // 7. How It Works
        item {
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "How It Works", subtitle = "Three steps from raw photo to studio master")
                Spacer(Modifier.height(12.dp))
                val steps = listOf(
                    "01" to ("Upload or Pick a Template" to "Choose any photo via Android Photo Picker or launch a Creative Studio canvas."),
                    "02" to ("Apply Real Edits & Styling" to "Adjust crops, ColorMatrix filters, text overlays, borders, or AI preview tools."),
                    "03" to ("Save & Export in Full Quality" to "Archive to My Projects, share via Android Share Sheet, or save directly to Gallery.")
                )
                steps.forEach { (num, pair) ->
                    LuxuryCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                num,
                                style = MaterialTheme.typography.headlineMedium,
                                color = PrimaryGold
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(pair.first, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(pair.second, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 8. Subscription Plans
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Studio Membership Plans", subtitle = "Managed transparently via database pricing")
                Spacer(Modifier.height(12.dp))
                plans.forEach { plan ->
                    LuxuryCard(
                        highlighted = plan.isPopular,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        onClick = onStartCreating
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.name, style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                                Text("${plan.monthlyCredits} credits / month", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(
                                text = if (plan.priceUsd <= 0.0) "Free" else "$${"%.2f".format(plan.priceUsd)}/mo",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(plan.featuresSummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // 9. Frequently Asked Questions
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Frequently Asked Questions")
                Spacer(Modifier.height(10.dp))
                val faqs = listOf(
                    "Which tools work immediately on-device?" to "All 10 Creative Studio tools (21–30) and all 10 Advanced Image Editing tools (31–40) perform real pixel processing and PNG/JPG/WEBP export on your device.",
                    "How do the 20 AI tools work?" to "AI Editing (1–10) and AI Generation (11–20) are routed through AiRepository. Because Firebase setup was declined, they display clearly labeled sample previews and refund any reserved credits until Firebase AI Logic is connected.",
                    "How does payment verification work?" to "Subscription orders create verifiable payment records marked PENDING_WEBHOOK_VERIFICATION until a server-side Stripe or Razorpay webhook is connected."
                )
                faqs.forEach { (q, a) ->
                    var expanded by remember { mutableStateOf(false) }
                    LuxuryCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        onClick = { expanded = !expanded }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(q, style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = PrimaryGold
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Text(
                                a,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 10. Footer
        item {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = BorderGoldMuted)
                Spacer(Modifier.height(16.dp))
                Text("AI PHOTO STUDIO", style = MaterialTheme.typography.titleSmall, color = SecondaryGold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Luxury Creative Suite • Local Room Persistence & Bitmap Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                GoldGradientButton(
                    text = "Launch Studio Workspace",
                    onClick = onStartCreating,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
