package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.StudioToolId
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoldGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isLoading: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ObsidianBlack,
            disabledContainerColor = BorderGoldMuted,
            disabledContentColor = TextSecondary
        ),
        contentPadding = PaddingValues(0.dp),
        shape = shape,
        modifier = modifier
            .heightIn(min = 50.dp)
            .background(
                brush = if (enabled) GoldGradientBrush else Brush.linearGradient(
                    listOf(BorderGoldMuted, BorderGoldMuted)
                ),
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = ObsidianBlack,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GoldOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, if (enabled) PrimaryGold else BorderGoldMuted),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGold),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.heightIn(min = 50.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = BorderStroke(
        width = if (highlighted) 1.5.dp else 1.dp,
        color = if (highlighted) PrimaryGold else BorderGoldMuted
    )
    val baseModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = baseModifier,
        shape = RoundedCornerShape(18.dp),
        color = CardObsidian,
        border = border,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel, color = PrimaryGold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun FirebaseNoticeBanner(
    notice: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF231C0E),
        border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Firebase AI Notice",
                tint = SecondaryGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI Features Require Firebase AI Logic",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryGold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun CreditBalancePill(
    credits: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(50),
        color = Color(0xFF231C0E),
        border = BorderStroke(1.dp, PrimaryGold)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Credits",
                tint = SecondaryGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$credits cr",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SecondaryGold
            )
        }
    }
}

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var containerWidthPx by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(
                (afterBitmap.width.toFloat() / afterBitmap.height.coerceAtLeast(1)).coerceIn(0.65f, 1.85f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianBlack)
            .border(1.dp, BorderGoldMuted, RoundedCornerShape(16.dp))
            .onSizeChanged { containerWidthPx = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    splitFraction = (splitFraction + dragAmount / containerWidthPx.toFloat()).coerceIn(0.05f, 0.95f)
                }
            }
    ) {
        // After image full width underneath
        Image(
            bitmap = afterBitmap.asImageBitmap(),
            contentDescription = "After Edit",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Before image clipped to left fraction
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(splitFraction)
                .clipToBounds()
        ) {
            Image(
                bitmap = beforeBitmap.asImageBitmap(),
                contentDescription = "Before Edit",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight()
                    .requiredWidth(with(androidx.compose.ui.platform.LocalDensity.current) {
                        containerWidthPx.toDp()
                    })
            )
        }

        // Gold vertical split line & handle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * splitFraction
            drawLine(
                color = PrimaryGold,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 4f
            )
            drawCircle(
                color = PrimaryGold,
                radius = 22f,
                center = Offset(x, size.height / 2f)
            )
            drawCircle(
                color = ObsidianBlack,
                radius = 12f,
                center = Offset(x, size.height / 2f)
            )
        }

        // Before & After pills
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            color = ObsidianBlack.copy(alpha = 0.78f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "BEFORE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            color = PrimaryGold,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "AFTER",
                color = ObsidianBlack,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF231C0E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                GoldGradientButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun StudioConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardObsidian,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusError else PrimaryGold,
                    contentColor = if (isDestructive) TextPrimary else ObsidianBlack
                )
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

fun iconForTool(tool: StudioToolId): ImageVector = when (tool) {
    StudioToolId.AI_ENHANCER -> Icons.Filled.AutoFixHigh
    StudioToolId.AI_UPSCALER -> Icons.Filled.HighQuality
    StudioToolId.AI_BG_REMOVER -> Icons.Filled.LayersClear
    StudioToolId.AI_BG_CHANGER -> Icons.Filled.Landscape
    StudioToolId.AI_OBJECT_REMOVER -> Icons.Filled.CleaningServices
    StudioToolId.AI_RETOUCH -> Icons.Filled.FaceRetouchingNatural
    StudioToolId.AI_FACE_ENHANCER -> Icons.Filled.Mood
    StudioToolId.AI_BLUR_BG -> Icons.Filled.BlurOn
    StudioToolId.AI_RESTORATION -> Icons.Filled.HistoryEdu
    StudioToolId.AI_COLOR_CORRECTION -> Icons.Filled.Palette

    StudioToolId.GEN_TEXT_TO_IMAGE -> Icons.Filled.AutoAwesome
    StudioToolId.GEN_AVATAR -> Icons.Filled.AccountCircle
    StudioToolId.GEN_CARTOON -> Icons.Filled.Brush
    StudioToolId.GEN_ANIME -> Icons.Filled.Animation
    StudioToolId.GEN_3D_CHARACTER -> Icons.Filled.ViewInAr
    StudioToolId.GEN_PORTRAIT -> Icons.Filled.Portrait
    StudioToolId.GEN_COUPLE -> Icons.Filled.Favorite
    StudioToolId.GEN_BABY -> Icons.Filled.ChildCare
    StudioToolId.GEN_HEADSHOT -> Icons.Filled.Badge
    StudioToolId.GEN_FASHION -> Icons.Filled.Checkroom

    StudioToolId.STUDIO_IG_DP -> Icons.Filled.AccountBox
    StudioToolId.STUDIO_YT_THUMB -> Icons.Filled.PlayCircle
    StudioToolId.STUDIO_IG_POST -> Icons.Filled.GridView
    StudioToolId.STUDIO_FESTIVAL -> Icons.Filled.Celebration
    StudioToolId.STUDIO_WEDDING -> Icons.Filled.FavoriteBorder
    StudioToolId.STUDIO_BIRTHDAY -> Icons.Filled.Cake
    StudioToolId.STUDIO_LOGO -> Icons.Filled.Diamond
    StudioToolId.STUDIO_COLLAGE -> Icons.Filled.Dashboard
    StudioToolId.STUDIO_FRAME -> Icons.Filled.FilterFrames
    StudioToolId.STUDIO_PASSPORT -> Icons.Filled.ContactMail

    StudioToolId.ADV_CROPPER -> Icons.Filled.Crop
    StudioToolId.ADV_RESIZER -> Icons.Filled.AspectRatio
    StudioToolId.ADV_COMPRESSOR -> Icons.Filled.Compress
    StudioToolId.ADV_FILTERS -> Icons.Filled.PhotoFilter
    StudioToolId.ADV_EFFECTS -> Icons.Filled.Flare
    StudioToolId.ADV_TEXT -> Icons.Filled.TextFields
    StudioToolId.ADV_STICKERS -> Icons.Filled.EmojiEmotions
    StudioToolId.ADV_BORDER -> Icons.Filled.BorderOuter
    StudioToolId.ADV_CONVERTER -> Icons.Filled.Transform
    StudioToolId.ADV_WATERMARK -> Icons.Filled.BrandingWatermark
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
