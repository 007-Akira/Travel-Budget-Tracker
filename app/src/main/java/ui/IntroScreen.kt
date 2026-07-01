package com.example.travelbudgettracker.ui

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelbudgettracker.R
import com.example.travelbudgettracker.ui.theme.Background
import com.example.travelbudgettracker.ui.theme.CardBorder
import com.example.travelbudgettracker.ui.theme.Gold
import com.example.travelbudgettracker.ui.theme.GoldLight
import com.example.travelbudgettracker.ui.theme.GrayText
import com.example.travelbudgettracker.ui.theme.WhiteText
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    onFinished: () -> Unit
) {
    var started by remember {
        mutableStateOf(false)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "intro_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intro_glow_scale"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.72f,
        animationSpec = tween(
            durationMillis = 760,
            easing = EaseOutCubic
        ),
        label = "intro_logo_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "intro_content_alpha"
    )

    val titleOffset by animateFloatAsState(
        targetValue = if (started) 0f else 18f,
        animationSpec = tween(
            durationMillis = 820,
            easing = EaseOutCubic
        ),
        label = "intro_title_offset"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1850)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Gold.copy(alpha = 0.22f),
                        Color(0xFF15120A),
                        Background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(184.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.10f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(contentAlpha)
        ) {
            Box(
                modifier = Modifier
                    .size(146.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(Color(0xFFF7FAFA))
                    .border(
                        width = 1.dp,
                        color = CardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.travel_budget_logo),
                    contentDescription = "Travel Budget Tracker",
                    modifier = Modifier.size(122.dp)
                )
            }

            Spacer(modifier = Modifier.height((26 + titleOffset).dp))

            Text(
                text = "TRAVEL BUDGET",
                color = Color(0xFF0B3D66),
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "TRACKER",
                color = Color(0xFF1FA36A),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(width = 92.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GoldLight,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
