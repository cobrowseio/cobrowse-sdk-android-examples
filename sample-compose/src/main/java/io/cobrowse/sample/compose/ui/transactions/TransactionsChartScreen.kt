package io.cobrowse.sample.compose.ui.transactions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.cobrowseRedacted
import io.cobrowse.cobrowseUnredacted
import io.cobrowse.sample.compose.R
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.data.model.Transaction
import kotlin.math.cos
import kotlin.math.sin

/**
 * Displays the balance and the recent transactions statistics.
 */
@Composable
fun TransactionsChartScreen(
    viewModelFactory: CobrowseViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel: TransactionsChartViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val horizontal = dimensionResource(R.dimen.activity_horizontal_margin)
    val vertical = dimensionResource(R.dimen.activity_vertical_margin)
    BoxWithConstraints(
        modifier = modifier.padding(start = horizontal, top = vertical, end = horizontal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(minOf(maxHeight, 400.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(io.cobrowse.sample.core.R.string.textview_balance_header),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.cobrowseUnredacted()
            )
            Text(
                text = stringResource(io.cobrowse.sample.core.R.string.transaction_amount, uiState.balance),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .cobrowseRedacted()
            )
            Spacer(modifier = Modifier.height(vertical))

            TransactionsPieChart(
                transactions = uiState.recentTransactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/**
 * A donut chart of the amounts spent per category, with the total in the middle.
 */
@Composable
fun TransactionsPieChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = remember(transactions) {
        transactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }
    val totalAmount = categoryTotals.values.sum()
    val icons = Transaction.Category.entries.associateWith { painterResource(it.icon) }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val holeDiameter = minOf(maxWidth, maxHeight) * 0.5f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .cobrowseUnredacted()
        ) {
            if (totalAmount <= 0.0) return@Canvas

            val radius = size.minDimension / 2
            val holeRadius = radius * 0.5f
            val sliceWidth = radius - holeRadius
            val sliceRadius = holeRadius + sliceWidth / 2
            val iconSize = minOf(32.dp.toPx(), sliceWidth * 0.6f)
            // The gap between slices, measured on the outer edge
            val gapDegrees = Math.toDegrees((4.dp.toPx() / radius).toDouble()).toFloat()

            // Start at the top and go clockwise
            var startAngle = -90f
            categoryTotals.forEach { (category, amount) ->
                val sweepAngle = (amount / totalAmount * 360).toFloat()
                drawArc(
                    color = Color(category.color),
                    startAngle = startAngle + gapDegrees / 2,
                    sweepAngle = (sweepAngle - gapDegrees).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(center.x - sliceRadius, center.y - sliceRadius),
                    size = Size(sliceRadius * 2, sliceRadius * 2),
                    style = Stroke(width = sliceWidth)
                )

                val iconAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val iconCenter = Offset(
                    x = center.x + sliceRadius * cos(iconAngle).toFloat(),
                    y = center.y + sliceRadius * sin(iconAngle).toFloat()
                )
                translate(iconCenter.x - iconSize / 2, iconCenter.y - iconSize / 2) {
                    with(icons.getValue(category)) {
                        draw(Size(iconSize, iconSize), colorFilter = ColorFilter.tint(Color.White))
                    }
                }
                startAngle += sweepAngle
            }

            // A translucent ring around the hole
            drawCircle(
                color = Color.White.copy(alpha = 100 / 255f),
                radius = holeRadius + radius * 0.025f,
                style = Stroke(width = radius * 0.05f)
            )
        }

        // The labels shrink to fit into the hole of a small chart, so do their line heights
        val textColor = MaterialTheme.colorScheme.onBackground
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(holeDiameter * 0.75f)
        ) {
            BasicText(
                text = stringResource(io.cobrowse.sample.core.R.string.total_spent_amount_header),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.2.em
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 14.sp),
                modifier = Modifier.cobrowseUnredacted()
            )
            BasicText(
                text = stringResource(io.cobrowse.sample.core.R.string.transaction_amount, totalAmount),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.2.em
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 28.sp),
                modifier = Modifier.cobrowseRedacted()
            )
            BasicText(
                text = stringResource(io.cobrowse.sample.core.R.string.total_spent_amount_footer),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.2.em
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 12.sp),
                modifier = Modifier.cobrowseUnredacted()
            )
        }
    }
}
