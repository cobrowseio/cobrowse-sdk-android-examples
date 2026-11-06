package io.cobrowse.sample.compose.ui.transactions

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.cobrowseRedacted
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.data.model.subtitle
import io.cobrowse.sample.data.model.transactionGroupHeader
import java.time.LocalDate

/**
 * Displays the list of transactions made in the past months.
 */
@Composable
fun TransactionsScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TransactionsViewModel = viewModel(factory = viewModelFactory)
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    TransactionsList(
        transactions = transactions,
        onTransactionClick = onTransactionClick,
        modifier = modifier
    )
}

@Composable
fun TransactionsList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedTransactions = remember(transactions) {
        transactions
            .groupBy { LocalDate.of(it.date.year, it.date.month, 1) }
            .toSortedMap(compareByDescending { it })
            .mapValues { (_, transactionsInMonth) -> transactionsInMonth.sortedByDescending { it.date } }
    }

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

    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    // Disables the stretch overscroll effect so the list doesn't "bounce" when a scroll
    // gesture reaches the top or bottom.
    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(consumeOverscrollConnection),
            contentPadding = PaddingValues(
                start = navigationBars.calculateStartPadding(layoutDirection),
                end = navigationBars.calculateEndPadding(layoutDirection),
                bottom = navigationBars.calculateBottomPadding()
            )
        ) {
            groupedTransactions.forEach { (month, transactionsInMonth) ->
                stickyHeader {
                    // Opaque like the sheet, so the scrolled items don't show through
                    Text(
                        text = month.transactionGroupHeader(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                }

                items(transactionsInMonth) { transaction ->
                    TransactionItem(
                        transaction = transaction,
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
    onClick: () -> Unit
) {
    val context = LocalContext.current

    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(transaction.category.icon),
                contentDescription = transaction.category.title,
                tint = Color(transaction.category.color),
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = {
            Text(transaction.title, modifier = Modifier.cobrowseRedacted())
        },
        supportingContent = {
            Text(transaction.subtitle(context), modifier = Modifier.cobrowseRedacted())
        },
        trailingContent = {
            Text(
                text = stringResource(io.cobrowse.sample.core.R.string.transaction_amount, transaction.amount),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.cobrowseRedacted()
            )
        },
        // The list is displayed in a bottom sheet, which has its own color
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
