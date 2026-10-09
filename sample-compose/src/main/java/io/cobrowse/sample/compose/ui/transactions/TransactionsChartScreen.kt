package io.cobrowse.sample.compose.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import io.cobrowse.sample.compose.R
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory

@Composable
fun TransactionsChartScreen(
    viewModelFactory: CobrowseViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel: TransactionsChartViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    val horizontal = dimensionResource(R.dimen.activity_horizontal_margin)
    val vertical = dimensionResource(R.dimen.activity_vertical_margin)
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(start = horizontal, top = vertical, end = horizontal, bottom = 0.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadData() }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                ) {
                    val availableHeight = this.maxHeight
                    val threshold = 400.dp

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .height(if (availableHeight > threshold) threshold else availableHeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(io.cobrowse.sample.core.R.string.textview_balance_header),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = String.format(
                                stringResource(io.cobrowse.sample.core.R.string.transaction_amount),
                                uiState.balance
                            ),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
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
        }
    }
}

@Composable
fun TransactionsPieChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = transactions
        .groupBy { it.category }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    
    val totalAmount = categoryTotals.values.sum()

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    val transactionsDictionary = transactions
                        .groupBy { it.category }
                        .mapValues { next -> next.value.sumOf { it.amount } }
                    val pieEntries = ArrayList<PieEntry>()
                    val colors = ArrayList<Int>()
                    for (transaction in transactionsDictionary) {
                        colors.add(transaction.key.color)

                        val icon = getDrawable(context, transaction.key.icon)
                        icon?.setTint(android.graphics.Color.WHITE)
                        pieEntries.add(PieEntry(transaction.value.toFloat(), icon))
                    }

                    val pieDataSet = PieDataSet(pieEntries, "type")
                    pieDataSet.valueTextSize = 12f
                    pieDataSet.colors = colors
                    pieDataSet.sliceSpace = 4f

                    val pieData = PieData(pieDataSet)
                    pieData.setDrawValues(false)

                    description = null
                    legend.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(getColor(context, android.R.color.transparent))
                    data = pieData
                    invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(io.cobrowse.sample.core.R.string.total_spent_amount_header),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = String.format(stringResource(io.cobrowse.sample.core.R.string.transaction_amount), totalAmount),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(io.cobrowse.sample.core.R.string.total_spent_amount_footer),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(widthDp = 720, heightDp = 600)
@Composable
fun TransactionsChartScreenPreview() {
    TransactionsChartScreen(CobrowseViewModelFactory())
}
