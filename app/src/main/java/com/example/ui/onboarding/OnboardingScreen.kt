package com.example.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currencies
import com.example.data.model.SupportedCurrency
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class OnboardingSlide(
    val title: String,
    val description: String,
    val iconRes: Int? = null,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
)

@Composable
fun OnboardingScreen(
    onFinish: (SupportedCurrency) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedCurrency by remember { mutableStateOf(Currencies.getByCode("PKR")) }

    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to GoldLedger",
            description = "The premier offline-first financial ledger. Elevate your financial control with elegant Black & Gold management.",
            iconRes = R.drawable.onboarding_hero
        ),
        OnboardingSlide(
            title = "Track Your Expenses",
            description = "Capture every expense and income source with custom categories, multi-account support, and instant analytics.",
            iconVector = Icons.Default.AccountBalanceWallet
        ),
        OnboardingSlide(
            title = "Manage Your Budget",
            description = "Set weekly or monthly category spending limits with smart threshold alerts to keep you financially disciplined.",
            iconVector = Icons.Default.PieChart
        ),
        OnboardingSlide(
            title = "Achieve Your Savings Goals",
            description = "Allocate funds toward emergency reserves, dreams, and investments with real-time target tracking.",
            iconVector = Icons.Default.Savings
        ),
        OnboardingSlide(
            title = "Select Your Currency",
            description = "Choose your primary currency. You can adjust this anytime in settings.",
            iconVector = Icons.Default.Stars
        )
    )

    Scaffold(
        containerColor = BlackPrimary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Step Indicator Dots
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in slides.indices) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == currentStep) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == currentStep) GoldPrimary else Color(0xFF333333))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Slide Content
                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier.weight(1f),
                    label = "onboarding_animation"
                ) { step ->
                    val slide = slides[step]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (step == 0 && slide.iconRes != null) {
                            Image(
                                painter = painterResource(id = slide.iconRes),
                                contentDescription = "GoldLedger Hero",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (step == 4) {
                            // Currency Selection Step
                            Text(
                                text = "Select Primary Currency",
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Default: PKR (Pakistani Rupee)",
                                color = GoldLight,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(Currencies.ALL) { currency ->
                                    val isSelected = currency.code == selectedCurrency.code
                                    Surface(
                                        onClick = { selectedCurrency = currency },
                                        color = if (isSelected) GoldContainer else BlackCard,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) GoldPrimary else BlackBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) GoldPrimary else Color(0xFF262626)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = currency.symbol,
                                                        color = if (isSelected) Color(0xFF141208) else GoldLight,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = currency.name,
                                                        color = TextWhite,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = currency.code,
                                                        color = TextMuted,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (slide.iconVector != null) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF2E2612), Color(0xFF171717))
                                        )
                                    )
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.iconVector,
                                    contentDescription = slide.title,
                                    tint = GoldLight,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        if (step != 4) {
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = slide.title,
                                color = TextWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = slide.description,
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        GoldOutlinedButton(
                            text = "Back",
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GoldButton(
                        text = if (currentStep == slides.lastIndex) "Get Started" else "Next",
                        onClick = {
                            if (currentStep == slides.lastIndex) {
                                onFinish(selectedCurrency)
                            } else {
                                currentStep++
                            }
                        },
                        modifier = Modifier.weight(if (currentStep > 0) 1.5f else 1f),
                        leadingIcon = if (currentStep == slides.lastIndex) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        testTagStr = "onboarding_next_button"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
