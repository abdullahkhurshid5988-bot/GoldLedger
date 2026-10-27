package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.data.model.SupportedCurrency
import com.example.ui.theme.BlackCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class CategoryPieData(
    val categoryName: String,
    val amountMinorUnits: Long,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    items: List<CategoryPieData>,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.amountMinorUnits }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(items) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(1f, animationSpec = tween(700))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlackCard)
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        if (items.isEmpty() || total == 0L) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No category spending data yet", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            Column {
                Text(
                    text = "Spending by Category",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 24.dp.toPx()
                        var startAngle = -90f

                        for (item in items) {
                            val sweepAngle = (item.amountMinorUnits.toFloat() / total.toFloat()) * 360f * progressAnim.value
                            drawArc(
                                color = item.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Spent",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = FinancialUtils.formatCurrency(total, currency),
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legends
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (item in items.take(6)) {
                        val pct = ((item.amountMinorUnits.toDouble() / total.toDouble()) * 100).toInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.categoryName} ($pct%)",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BarChartItem(
    val label: String,
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long
)

@Composable
fun IncomeVsExpenseBarChart(
    data: List<BarChartItem>,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { maxOf(it.incomeMinorUnits, it.expenseMinorUnits) }?.coerceAtLeast(1L) ?: 1L
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(600))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlackCard)
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs Expenses",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transaction data in this period", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (item in data) {
                        val incomeFrac = (item.incomeMinorUnits.toFloat() / maxVal.toFloat()) * animProgress.value
                        val expenseFrac = (item.expenseMinorUnits.toFloat() / maxVal.toFloat()) * animProgress.value

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.height(110.dp)
                            ) {
                                // Income bar
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height((100 * incomeFrac.coerceIn(0.04f, 1f)).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(IncomeGreen)
                                )
                                // Expense bar
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height((100 * expenseFrac.coerceIn(0.04f, 1f)).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(ExpenseRed)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TrendPoint(
    val label: String,
    val valueMinorUnits: Long
)

@Composable
fun TrendLineChart(
    points: List<TrendPoint>,
    title: String,
    currency: SupportedCurrency,
    lineColor: Color = GoldPrimary,
    modifier: Modifier = Modifier
) {
    val maxVal = points.maxOfOrNull { it.valueMinorUnits }?.coerceAtLeast(1L) ?: 1L
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(600))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlackCard)
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add more transactions to view trend line", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height - 24.dp.toPx()
                        val stepX = w / (points.size - 1)

                        // Draw Grid lines
                        drawLine(
                            color = Color(0xFF2A2A2A),
                            start = Offset(0f, 0f),
                            end = Offset(w, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF2A2A2A),
                            start = Offset(0f, h / 2),
                            end = Offset(w, h / 2),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF2A2A2A),
                            start = Offset(0f, h),
                            end = Offset(w, h),
                            strokeWidth = 1.dp.toPx()
                        )

                        val path = Path()
                        val fillPath = Path()

                        for ((i, pt) in points.withIndex()) {
                            val x = i * stepX
                            val fraction = (pt.valueMinorUnits.toFloat() / maxVal.toFloat()) * animProgress.value
                            val y = h - (h * fraction)

                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, h)
                                fillPath.lineTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevPt = points[i - 1]
                                val prevFrac = (prevPt.valueMinorUnits.toFloat() / maxVal.toFloat()) * animProgress.value
                                val prevY = h - (h * prevFrac)
                                val cX1 = prevX + (x - prevX) / 2
                                val cY1 = prevY
                                val cX2 = prevX + (x - prevX) / 2
                                val cY2 = y
                                path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                                fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                            }
                        }

                        // Close fill path
                        fillPath.lineTo((points.size - 1) * stepX, h)
                        fillPath.close()

                        // Draw gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.3f),
                                    lineColor.copy(alpha = 0.0f)
                                )
                            )
                        )

                        // Draw main stroke
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw points
                        for ((i, pt) in points.withIndex()) {
                            val x = i * stepX
                            val fraction = (pt.valueMinorUnits.toFloat() / maxVal.toFloat()) * animProgress.value
                            val y = h - (h * fraction)
                            drawCircle(
                                color = Color(0xFF141414),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // X-Axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (pt in points) {
                        Text(
                            text = pt.label,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
