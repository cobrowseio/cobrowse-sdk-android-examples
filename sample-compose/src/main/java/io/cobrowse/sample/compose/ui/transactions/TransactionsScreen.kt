package io.cobrowse.sample.compose.ui.transactions

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.data.TransactionDrawables
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.data.model.subtitle
import io.cobrowse.sample.data.model.transactionGroupHeader
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TransactionsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
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
                    Button(onClick = { viewModel.loadTransactions() }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                TransactionsList(
                    transactions = uiState.transactions,
                    onTransactionClick = onTransactionClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    val drawables = remember { TransactionDrawables() }

    val groupedTransactions = transactions
        .groupBy {
            LocalDate.of(it.date.year, it.date.month, 1)
        }
        .asIterable()
        .sortedByDescending { it.key }

    // When the list reaches the top/bottom of its content, any leftover scroll/fling would
    // normally bubble up to the enclosing BottomSheetScaffold, which interprets it as a drag on
    // the sheet itself - causing the whole sheet to visibly nudge and spring back ("bounce").
    // Swallowing the leftover delta/velocity here keeps that gesture local to the list.
    val consumeOverscrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = available

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity = available
        }
    }

    // Disables the stretch/glow overscroll effect so the list doesn't "bounce" when a scroll
    // gesture reaches the top or bottom.
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(consumeOverscrollConnection),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            groupedTransactions
                .forEach { (month, transactionsInMonth) ->
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = month.transactionGroupHeader(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }

                    items(transactionsInMonth) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            drawable = drawables.getDrawable(
                                LocalContext.current,
                                transaction.category
                            ),
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    drawable: Drawable?,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .clickable(onClick = onClick),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (drawable == null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(transaction.category.color),
                            shape = CircleShape
                        )
                )
            } else {
                AsyncImage(
                    model = drawable,
                    contentDescription = transaction.category.name,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.subtitle(context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = String.format(stringResource(io.cobrowse.sample.core.R.string.transaction_amount), transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun TransactionsScreenPreview() {
    TransactionsScreen(CobrowseViewModelFactory(), {})
}
