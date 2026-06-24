package com.vynik.wallshift.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vynik.wallshift.ui.theme.*

// ── Glass Card ─────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    border: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassBackground = Brush.linearGradient(
        colors = listOf(
            Color(0x26FFFFFF),
            Color(0x0DFFFFFF)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassBackground)
            .then(
                if (border) Modifier.border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x1AFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                ) else Modifier
            ),
        content = content
    )
}

// ── Ambient Orbs (background decoration) ───────────────────────────────────

@Composable
fun AmbientOrbs(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbOffset"
    )

    Box(modifier = modifier) {
        // Violet orb - top right
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (160 + offset * 20).dp, y = (-60 + offset * 30).dp)
                .blur(80.dp)
                .background(
                    Color(0x55563EAA),
                    CircleShape
                )
        )
        // Ice blue orb - bottom left
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80 + offset * 15).dp, y = (400 + offset * -20).dp)
                .blur(80.dp)
                .background(
                    Color(0x3304A5D1),
                    CircleShape
                )
        )
        // Magenta spark - center
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (100 + offset * -10).dp, y = (250 + offset * 15).dp)
                .blur(60.dp)
                .background(
                    Color(0x228B1DA4),
                    CircleShape
                )
        )
    }
}

// ── Toggle Switch (glass style) ─────────────────────────────────────────────

@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AccentPrimary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = TextSecondary,
            uncheckedTrackColor = GlassWhite,
            uncheckedBorderColor = GlassBorder
        )
    )
}

// ── Primary Action Button ────────────────────────────────────────────────────

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) listOf(AccentPrimary, AccentTertiary)
        else listOf(TextDisabled, TextDisabled)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(gradient)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

// ── Section Label ─────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        letterSpacing = 1.5.sp,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

// ── Stat Pill ────────────────────────────────────────────────────────────

@Composable
fun StatPill(
    value: String,
    label: String,
    accentColor: Color = AccentPrimary,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.padding(4.dp), cornerRadius = 14.dp) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Glow Divider ─────────────────────────────────────────────────────────

@Composable
fun GlowDivider(modifier: Modifier = Modifier, color: Color = AccentPrimary) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}
