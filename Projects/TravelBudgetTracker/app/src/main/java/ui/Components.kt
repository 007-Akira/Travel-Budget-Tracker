package com.example.travelbudgettracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import com.example.travelbudgettracker.ui.theme.Success
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelbudgettracker.ui.theme.Background
import com.example.travelbudgettracker.ui.theme.CardBorder
import com.example.travelbudgettracker.ui.theme.Danger
import com.example.travelbudgettracker.ui.theme.Gold
import com.example.travelbudgettracker.ui.theme.GoldDark
import com.example.travelbudgettracker.ui.theme.GoldLight
import com.example.travelbudgettracker.ui.theme.GrayText
import com.example.travelbudgettracker.ui.theme.HintText
import com.example.travelbudgettracker.ui.theme.Surface
import com.example.travelbudgettracker.ui.theme.Surface2
import com.example.travelbudgettracker.ui.theme.WhiteText

/*
    Components.kt - Part 1

    Contains:
    - Premium design constants
    - Gradient helpers
    - Animated press wrapper
    - PremiumCard
    - PremiumTextField
    - PremiumButton

    Paste Part 2 directly below this file later.
*/

@Immutable
object PremiumDimens {
    val ScreenPadding = 20.dp
    val CardRadius = 30.dp
    val ButtonRadius = 22.dp
    val FieldRadius = 22.dp
    val ChipRadius = 100.dp

    val Tiny = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Huge = 32.dp
}

@Immutable
object PremiumText {
    val LargeTitle = TextStyle(
        color = WhiteText,
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.8).sp,
        lineHeight = 38.sp
    )

    val Title = TextStyle(
        color = WhiteText,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        lineHeight = 31.sp
    )

    val SectionTitle = TextStyle(
        color = WhiteText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp
    )

    val CardTitle = TextStyle(
        color = WhiteText,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp
    )

    val Body = TextStyle(
        color = GrayText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp
    )

    val Label = TextStyle(
        color = Gold,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp
    )

    val Small = TextStyle(
        color = GrayText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )

    val Amount = TextStyle(
        color = WhiteText,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.8).sp
    )

    val Button = TextStyle(
        color = Background,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp
    )
}

@Stable
fun goldGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            GoldLight,
            Gold,
            GoldDark
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}

@Stable
fun darkGlassGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF202020),
            Color(0xFF141414),
            Color(0xFF101010)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}

@Stable
fun premiumBorderGradient(isFocused: Boolean = false): Brush {
    return if (isFocused) {
        Brush.linearGradient(
            colors = listOf(
                GoldLight,
                Gold,
                GoldDark
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                CardBorder,
                Color(0xFF202020),
                CardBorder
            )
        )
    }
}

@Stable
fun softGoldGlow(): Brush {
    return Brush.radialGradient(
        colors = listOf(
            Gold.copy(alpha = 0.24f),
            Gold.copy(alpha = 0.08f),
            Color.Transparent
        )
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AnimatedPressBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 420f
        ),
        label = "premium_press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                when {
                    onLongClick != null -> Modifier.combinedClickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            onClick?.invoke()
                        },
                        onLongClick = onLongClick
                    )

                    onClick != null -> Modifier.clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )

                    else -> Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = PremiumDimens.CardRadius,
    borderColor: Color = CardBorder,
    backgroundBrush: Brush = darkGlassGradient(),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    glow: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AnimatedPressBox(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (glow) 18.dp else 8.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    clip = false
                )
        ) {
            if (glow) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(softGoldGlow())
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundBrush)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> Danger
            focused -> Gold
            else -> CardBorder
        },
        label = "premium_text_field_border"
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            isError -> Danger
            focused -> GoldLight
            else -> Gold
        },
        label = "premium_text_field_label"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = PremiumText.Label.copy(color = labelColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (singleLine) 58.dp else 118.dp)
                .shadow(
                    elevation = if (focused) 14.dp else 4.dp,
                    shape = RoundedCornerShape(PremiumDimens.FieldRadius),
                    clip = false
                )
                .clip(RoundedCornerShape(PremiumDimens.FieldRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Surface2.copy(alpha = 0.96f),
                            Surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = if (focused || isError) 1.4.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(PremiumDimens.FieldRadius)
                )
                .padding(horizontal = 18.dp, vertical = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        leadingIcon()
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = PremiumText.Body.copy(
                                color = HintText,
                                fontSize = 15.sp
                            )
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = singleLine,
                        interactionSource = interactionSource,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(GoldLight),
                        textStyle = LocalTextStyle.current.merge(
                            TextStyle(
                                color = WhiteText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (trailingIcon != null) {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        trailingIcon()
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
    backgroundBrush: Brush = goldGradient(),
    textColor: Color = Background,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 24.dp,
        vertical = 17.dp
    )
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled && !loading) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "premium_button_alpha"
    )

    AnimatedPressBox(
        modifier = modifier.then(
            if (fullWidth) Modifier.fillMaxWidth() else Modifier
        ),
        enabled = enabled && !loading,
        onClick = {
            if (enabled && !loading) onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (fullWidth) Modifier.fillMaxWidth() else Modifier
                )
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(PremiumDimens.ButtonRadius),
                    clip = false
                )
                .clip(RoundedCornerShape(PremiumDimens.ButtonRadius))
                .background(backgroundBrush)
                .alpha(alpha)
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Background,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text.uppercase(),
                    style = PremiumText.Button.copy(color = textColor),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PremiumOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    AnimatedPressBox(
        modifier = modifier.then(
            if (fullWidth) Modifier.fillMaxWidth() else Modifier
        ),
        enabled = enabled,
        onClick = {
            if (enabled) onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (fullWidth) Modifier.fillMaxWidth() else Modifier
                )
                .clip(RoundedCornerShape(PremiumDimens.ButtonRadius))
                .background(Surface)
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        brush = premiumBorderGradient(isFocused = true)
                    ),
                    shape = RoundedCornerShape(PremiumDimens.ButtonRadius)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = PremiumText.Button.copy(
                    color = Gold,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GoldMiniButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedPressBox(
        modifier = modifier,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(goldGradient())
                .padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Background,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            )
        }
    }
}
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = PremiumText.SectionTitle
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = PremiumText.Small
                )
            }
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            GoldMiniButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun PremiumHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = true,
        contentPadding = PaddingValues(24.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF242018),
                Color(0xFF151515),
                Color(0xFF0B0B0B)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Gold.copy(alpha = 0.14f)
                        )
                        .border(
                            width = 1.dp,
                            color = Gold.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText.uppercase(),
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            Text(
                text = title,
                style = PremiumText.LargeTitle
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = PremiumText.Body
                )
            }
        }
    }
}

@Composable
fun PremiumChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    enabled: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Gold else Surface2,
        label = "premium_chip_background"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) Background else GrayText,
        label = "premium_chip_content"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) GoldLight else CardBorder,
        label = "premium_chip_border"
    )

    AnimatedPressBox(
        modifier = modifier,
        enabled = enabled,
        onClick = {
            if (enabled) onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(PremiumDimens.ChipRadius))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(PremiumDimens.ChipRadius)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!emoji.isNullOrBlank()) {
                Text(
                    text = emoji,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.size(7.dp))
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChipRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            PremiumChip(
                text = category,
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(category)
                },
                emoji = categoryEmoji(category)
            )
        }
    }
}

fun categoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food" -> "🍔"
        "travel" -> "✈️"
        "stay" -> "🏨"
        "shopping" -> "🛍️"
        "fuel" -> "⛽"
        "medical" -> "💊"
        "ticket" -> "🎟️"
        "other" -> "✨"
        else -> "💎"
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String = "💰",
    highlighted: Boolean = false
) {
    PremiumCard(
        modifier = modifier,
        cornerRadius = 28.dp,
        glow = highlighted,
        contentPadding = PaddingValues(18.dp),
        backgroundBrush = if (highlighted) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF2C2414),
                    Color(0xFF191919),
                    Color(0xFF111111)
                )
            )
        } else {
            darkGlassGradient()
        },
        borderColor = if (highlighted) Gold.copy(alpha = 0.55f) else CardBorder
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (highlighted) {
                                Gold.copy(alpha = 0.18f)
                            } else {
                                Surface2
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (highlighted) {
                                Gold.copy(alpha = 0.35f)
                            } else {
                                CardBorder
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 19.sp
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = title,
                    color = GrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = amount,
                style = PremiumText.Amount.copy(
                    fontSize = 28.sp,
                    color = if (highlighted) GoldLight else WhiteText
                )
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = subtitle,
                    style = PremiumText.Small
                )
            }
        }
    }
}

@Composable
fun CompactStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String = "✦",
    valueColor: Color = WhiteText
) {
    PremiumCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(16.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1C1C1C),
                Color(0xFF121212)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    color = GrayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

@Composable
fun ExpenseCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    category: String = "Other",
    dateText: String = "",
    splitText: String? = null,
    receiptAvailable: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Gold.copy(alpha = 0.22f),
                                    GoldDark.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Gold.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categoryEmoji(category),
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = PremiumText.CardTitle,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = buildString {
                            append(category)

                            if (dateText.isNotBlank()) {
                                append("  •  ")
                                append(dateText)
                            }
                        },
                        style = PremiumText.Small
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = amount,
                        color = GoldLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp
                    )

                }
            }

            if (!splitText.isNullOrBlank() || receiptAvailable) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CardBorder,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!splitText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Success.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = Success.copy(alpha = 0.28f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = splitText,
                                color = Success,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (receiptAvailable) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = Gold.copy(alpha = 0.28f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Receipt attached",
                                color = Gold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripCard(
    tripName: String,
    amount: String? = null,
    modifier: Modifier = Modifier,
    subtitle: String = "Travel budget",
    transactionCount: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = true,
        contentPadding = PaddingValues(22.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF241F15),
                Color(0xFF171717),
                Color(0xFF0D0D0D)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        ),
        borderColor = Gold.copy(alpha = 0.32f),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Gold.copy(alpha = 0.16f))
                        .border(
                            width = 1.dp,
                            color = Gold.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✈️",
                        fontSize = 27.sp
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tripName,
                        style = PremiumText.CardTitle.copy(
                            fontSize = 21.sp
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = subtitle,
                        style = PremiumText.Small
                    )
                }
            }

            if (!amount.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = amount,
                    style = PremiumText.Amount.copy(
                        color = GoldLight,
                        fontSize = 34.sp
                    )
                )
            }

            if (!transactionCount.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = transactionCount,
                    style = PremiumText.Small
                )
            }
        }
    }
}

@Composable
fun GoldFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "+",
    enabled: Boolean = true
) {
    AnimatedPressBox(
        modifier = modifier,
        enabled = enabled,
        onClick = {
            if (enabled) onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .background(goldGradient())
                .border(
                    width = 1.dp,
                    color = GoldLight.copy(alpha = 0.75f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Background,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        CardBorder,
                        Gold.copy(alpha = 0.18f),
                        CardBorder,
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String = "✨",
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        contentPadding = PaddingValues(28.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1C1C1C),
                Color(0xFF111111),
                Color(0xFF0B0B0B)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = Gold.copy(alpha = 0.30f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 34.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = PremiumText.SectionTitle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = PremiumText.Body,
                textAlign = TextAlign.Center
            )

            if (!actionText.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(22.dp))

                PremiumButton(
                    text = actionText,
                    onClick = onActionClick
                )
            }
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading premium experience..."
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 32.dp,
        contentPadding = PaddingValues(26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Gold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = PremiumText.Body,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DebtCard(
    personName: String,
    amount: String,
    modifier: Modifier = Modifier,
    category: String = "Other",
    paid: Boolean = false,
    youOwe: Boolean = true,
    onTogglePaid: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val statusColor = if (paid) Success else if (youOwe) Danger else Gold
    val statusText = when {
        paid -> "Paid"
        youOwe -> "You owe"
        else -> "Owes you"
    }

    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp),
        borderColor = statusColor.copy(alpha = 0.32f),
        onClick = onClick,
        onLongClick = onLongClick,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                statusColor.copy(alpha = 0.08f),
                Color(0xFF151515),
                Color(0xFF101010)
            )
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(statusColor.copy(alpha = 0.13f))
                    .border(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (youOwe) "↗" else "↙",
                    color = statusColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = personName,
                    style = PremiumText.CardTitle,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "$category  •  $statusText",
                    style = PremiumText.Small.copy(
                        color = statusColor
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = amount,
                    color = statusColor,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                )

                if (onTogglePaid != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedPressBox(
                        onClick = onTogglePaid
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = statusColor.copy(alpha = 0.32f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (paid) "Mark unpaid" else "Mark paid",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumDialogContainer(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 36.dp,
        glow = true,
        contentPadding = PaddingValues(24.dp),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF211D16),
                Color(0xFF151515),
                Color(0xFF0C0C0C)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        ),
        borderColor = Gold.copy(alpha = 0.30f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = PremiumText.Title
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = subtitle,
                            style = PremiumText.Body
                        )
                    }
                }

                if (onClose != null) {
                    AnimatedPressBox(
                        onClick = onClose
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Surface2)
                                .border(
                                    width = 1.dp,
                                    color = CardBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                color = GrayText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PremiumDivider()

            Spacer(modifier = Modifier.height(22.dp))

            content()
        }
    }
}

@Composable
fun ReceiptPreviewCard(
    imageLabel: String = "Receipt attached",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(18.dp),
        borderColor = Gold.copy(alpha = 0.26f),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Gold.copy(alpha = 0.08f),
                Surface,
                Color(0xFF101010)
            )
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Gold.copy(alpha = 0.14f))
                    .border(
                        width = 1.dp,
                        color = Gold.copy(alpha = 0.32f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧾",
                    fontSize = 25.sp
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = imageLabel,
                    style = PremiumText.CardTitle.copy(
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to view receipt proof",
                    style = PremiumText.Small
                )
            }

            Text(
                text = "View",
                color = Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun PremiumAmountBlock(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    emoji: String = "💰",
    highlighted: Boolean = true
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = highlighted,
        contentPadding = PaddingValues(24.dp),
        borderColor = if (highlighted) Gold.copy(alpha = 0.38f) else CardBorder,
        backgroundBrush = if (highlighted) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF2C2414),
                    Color(0xFF181818),
                    Color(0xFF0C0C0C)
                )
            )
        } else {
            darkGlassGradient()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = label.uppercase(),
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = amount,
                style = PremiumText.LargeTitle.copy(
                    color = if (highlighted) GoldLight else WhiteText,
                    fontSize = 40.sp,
                    lineHeight = 44.sp
                )
            )
        }
    }
}

@Composable
fun PremiumInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String = "•",
    valueColor: Color = WhiteText
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface.copy(alpha = 0.86f))
            .border(
                width = 1.dp,
                color = CardBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = label,
            style = PremiumText.Body,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PremiumTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = PremiumDimens.ScreenPadding,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            AnimatedPressBox(
                onClick = onBack
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Surface2)
                        .border(
                            width = 1.dp,
                            color = CardBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = Gold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.size(14.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = PremiumText.SectionTitle,
                maxLines = 1
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = PremiumText.Small
                )
            }
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            GoldMiniButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun PremiumScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(Background)
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Gold.copy(alpha = 0.13f),
                            Gold.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldDark.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun PremiumWarningText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = Danger,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 17.sp
    )
}

@Composable
fun PremiumSuccessText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = Success,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 17.sp
    )
}
