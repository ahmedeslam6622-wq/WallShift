package com.vynik.wallshift.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.vynik.wallshift.data.model.RotationInterval
import com.vynik.wallshift.data.model.WallpaperImage
import com.vynik.wallshift.data.model.WallpaperTarget
import com.vynik.wallshift.ui.MainViewModel
import com.vynik.wallshift.ui.components.*
import com.vynik.wallshift.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showIntervalSheet by remember { mutableStateOf(false) }
    var showTargetSheet by remember { mutableStateOf(false) }

    // Snackbar
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    // Photo picker (multi-select)
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    // Permission
    val mediaPermission = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            // Animated ambient background orbs
            AmbientOrbs(modifier = Modifier.fillMaxSize())

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 60.dp, bottom = 40.dp)
            ) {

                // ── Header ─────────────────────────────────────────────
                item {
                    HeaderSection(
                        activeCount = state.images.count { it.isActive },
                        lastChanged = state.settings.lastChangedAt
                    )
                }

                // ── Master toggle card ─────────────────────────────────
                item {
                    MasterToggleCard(
                        isEnabled = state.settings.isEnabled,
                        onToggle = viewModel::setEnabled,
                        onApplyNow = viewModel::applyNow,
                        hasImages = state.images.any { it.isActive }
                    )
                }

                // ── Stats row ──────────────────────────────────────────
                item {
                    StatsRow(
                        imageCount = state.images.count { it.isActive },
                        intervalHours = state.settings.intervalHours,
                        target = state.settings.target
                    )
                }

                // ── Settings card ──────────────────────────────────────
                item {
                    SettingsCard(
                        settings = state.settings,
                        onIntervalClick = { showIntervalSheet = true },
                        onTargetClick = { showTargetSheet = true },
                        onShuffleToggle = viewModel::setShuffleMode
                    )
                }

                // ── Image gallery ──────────────────────────────────────
                item {
                    SectionLabel(text = "Wallpaper Pool")
                }

                item {
                    AddImagesButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }

                // Image grid
                items(
                    items = state.images,
                    key = { it.id }
                ) { image ->
                    ImageCard(
                        image = image,
                        isCurrentIndex = state.settings.currentIndex == state.images.indexOf(image),
                        onApply = { viewModel.applySpecific(image) },
                        onToggle = { viewModel.toggleImageActive(image) },
                        onRemove = { viewModel.removeImage(image) }
                    )
                }

                if (state.images.isEmpty()) {
                    item { EmptyGalleryHint() }
                }
            }
        }
    }

    // ── Interval bottom sheet ──────────────────────────────────────────────
    if (showIntervalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIntervalSheet = false },
            containerColor = Color(0xFF0F1E3A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = GlassBorder) }
        ) {
            IntervalPickerSheet(
                current = state.settings.intervalHours,
                onSelect = {
                    viewModel.setInterval(it)
                    showIntervalSheet = false
                }
            )
        }
    }

    // ── Target bottom sheet ────────────────────────────────────────────────
    if (showTargetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTargetSheet = false },
            containerColor = Color(0xFF0F1E3A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = GlassBorder) }
        ) {
            TargetPickerSheet(
                current = state.settings.target,
                onSelect = {
                    viewModel.setTarget(it)
                    showTargetSheet = false
                }
            )
        }
    }
}

// ── Header Section ────────────────────────────────────────────────────────

@Composable
private fun HeaderSection(activeCount: Int, lastChanged: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "headerGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        AccentPrimary.copy(alpha = glowAlpha),
                        CircleShape
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "WallShift",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.horizontalGradient(
                        listOf(Color.White, AccentSecondary)
                    )
                ),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "by Vynik",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (lastChanged > 0) {
                val fmt = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())
                "Last changed ${fmt.format(Date(lastChanged))}"
            } else {
                "Infinite wallpaper rotation"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

// ── Master Toggle Card ────────────────────────────────────────────────────

@Composable
private fun MasterToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onApplyNow: () -> Unit,
    hasImages: Boolean
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status orb
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isEnabled) AccentPrimary.copy(alpha = 0.2f)
                        else GlassWhite,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Filled.AutoMode else Icons.Outlined.AutoMode,
                    contentDescription = null,
                    tint = if (isEnabled) AccentPrimary else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnabled) "Rotation Active" else "Rotation Paused",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isEnabled) "Wallpapers changing automatically"
                    else "Tap to start auto-rotation",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            GlassSwitch(checked = isEnabled, onCheckedChange = onToggle)
        }

        if (isEnabled && hasImages) {
            GlowDivider(color = AccentPrimary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onApplyNow)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Apply next",
                    tint = AccentSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Apply Next Now",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentSecondary
                )
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(imageCount: Int, intervalHours: Long, target: WallpaperTarget) {
    val intervalLabel = RotationInterval.values()
        .firstOrNull { it.hours == intervalHours }
        ?.label?.replace(" — Recommended", "")
        ?: "${intervalHours}h"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatPill(
            value = imageCount.toString(),
            label = "Images",
            accentColor = AccentPrimary,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            value = intervalLabel,
            label = "Interval",
            accentColor = AccentSecondary,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            value = when (target) {
                WallpaperTarget.HOME -> "Home"
                WallpaperTarget.LOCK -> "Lock"
                WallpaperTarget.BOTH -> "Both"
            },
            label = "Screen",
            accentColor = AccentTertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Settings Card ─────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    settings: com.vynik.wallshift.data.model.AppSettings,
    onIntervalClick: () -> Unit,
    onTargetClick: () -> Unit,
    onShuffleToggle: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {

            // Interval
            SettingsRow(
                icon = Icons.Outlined.Timer,
                label = "Change interval",
                value = RotationInterval.values()
                    .firstOrNull { it.hours == settings.intervalHours }
                    ?.label?.replace(" — Recommended", "") ?: "${settings.intervalHours}h",
                onClick = onIntervalClick,
                accentColor = AccentPrimary
            )

            HorizontalDivider(color = GlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Target
            SettingsRow(
                icon = Icons.Outlined.PhoneAndroid,
                label = "Apply to screen",
                value = settings.target.label,
                onClick = onTargetClick,
                accentColor = AccentSecondary
            )

            HorizontalDivider(color = GlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Shuffle
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AccentTertiary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shuffle, null, tint = AccentTertiary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Shuffle mode", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Randomize order", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                GlassSwitch(
                    checked = settings.shuffleMode,
                    onCheckedChange = onShuffleToggle
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(value, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(20.dp))
    }
}

// ── Add Images Button ─────────────────────────────────────────────────────

@Composable
private fun AddImagesButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AccentPrimary.copy(alpha = 0.15f),
                        AccentSecondary.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(AccentPrimary.copy(0.4f), AccentSecondary.copy(0.3f))),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, null, tint = AccentPrimary, modifier = Modifier.size(22.dp))
            Text(
                "Add Images",
                style = MaterialTheme.typography.titleMedium,
                color = AccentPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Image Card ────────────────────────────────────────────────────────────

@Composable
private fun ImageCard(
    image: WallpaperImage,
    isCurrentIndex: Boolean,
    onApply: () -> Unit,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        cornerRadius = 16.dp,
        border = isCurrentIndex
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (!image.isActive) Modifier.graphicsLayer { alpha = 0.4f }
                            else Modifier
                        )
                )
                if (isCurrentIndex) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(AccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = image.displayName.ifEmpty { "Image ${image.id}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (image.isActive) TextPrimary else TextDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (image.isActive) "In rotation" else "Excluded",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (image.isActive) SuccessGreen else TextDisabled
                )
            }

            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                null, tint = TextSecondary, modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Apply this
                SmallActionButton(
                    text = "Apply",
                    icon = Icons.Filled.WallpaperSharp,
                    color = AccentPrimary,
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                )
                // Toggle
                SmallActionButton(
                    text = if (image.isActive) "Exclude" else "Include",
                    icon = if (image.isActive) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    color = AccentSecondary,
                    onClick = onToggle,
                    modifier = Modifier.weight(1f)
                )
                // Remove
                SmallActionButton(
                    text = "Remove",
                    icon = Icons.Filled.Delete,
                    color = ErrorRed,
                    onClick = onRemove,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────

@Composable
private fun EmptyGalleryHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Collections,
                null,
                tint = TextDisabled,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No images yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap Add Images to build your rotation pool",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled
            )
        }
    }
}

// ── Interval Picker Sheet ─────────────────────────────────────────────────

@Composable
private fun IntervalPickerSheet(current: Long, onSelect: (RotationInterval) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            "Change Interval",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        Text(
            "Research shows people psychologically tire of the same wallpaper after 3–7 days. We recommend 3 days.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))

        RotationInterval.values().forEach { interval ->
            val isSelected = interval.hours == current
            val isRecommended = interval == RotationInterval.THREE_DAYS

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(interval) }
                    .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            interval.label.replace(" — Recommended", ""),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) AccentPrimary else TextPrimary
                        )
                        if (isRecommended) {
                            Box(
                                modifier = Modifier
                                    .background(AccentPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Recommended", style = MaterialTheme.typography.labelSmall, color = AccentPrimary)
                            }
                        }
                    }
                }
                if (isSelected) {
                    Icon(Icons.Filled.Check, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                }
            }
            HorizontalDivider(color = GlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

// ── Target Picker Sheet ───────────────────────────────────────────────────

@Composable
private fun TargetPickerSheet(current: WallpaperTarget, onSelect: (WallpaperTarget) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            "Apply Wallpaper To",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        WallpaperTarget.values().forEach { target ->
            val isSelected = target == current
            val icon = when (target) {
                WallpaperTarget.HOME -> Icons.Outlined.Home
                WallpaperTarget.LOCK -> Icons.Outlined.Lock
                WallpaperTarget.BOTH -> Icons.Outlined.PhoneAndroid
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(target) }
                    .background(if (isSelected) AccentSecondary.copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = if (isSelected) AccentSecondary else TextSecondary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    target.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) AccentSecondary else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Filled.Check, null, tint = AccentSecondary, modifier = Modifier.size(20.dp))
                }
            }
            HorizontalDivider(color = GlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}
