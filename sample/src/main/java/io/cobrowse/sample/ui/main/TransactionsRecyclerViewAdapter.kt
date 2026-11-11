package io.cobrowse.sample.ui.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.R
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.data.model.subtitle
import io.cobrowse.sample.data.model.transactionGroupHeader
import io.cobrowse.sample.databinding.CellTransactionBinding
import io.cobrowse.sample.databinding.CellTransactionMonthBinding
import io.cobrowse.sample.ui.main.TransactionsRecyclerViewAdapter.ListItem.Companion.TYPE_MONTH_AND_YEAR
import io.cobrowse.sample.ui.main.TransactionsRecyclerViewAdapter.ListItem.Companion.TYPE_TRANSACTION
import java.time.LocalDate

/**
 * [RecyclerView.Adapter] that can display a [Transaction].
 */
class TransactionsRecyclerViewAdapter(private val values: List<ListItem>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    CobrowseIO.Redacted {

    /**
     * Keeps all displayed [RecyclerView.ViewHolder] items so they can be redacted in Cobrowse.
     */
    private val displayedCells = HashSet<RecyclerView.ViewHolder>()

    /**
     * A simple and naive drawable cache.
     */
    private val drawables = HashMap<Int, Drawable?>()

    private var onTransactionSelected: ((Transaction) -> Unit) = {}

    fun setOnTransactionSelected(block: (Transaction) -> Unit) { onTransactionSelected = block }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MONTH_AND_YEAR -> createMonthAndYearViewHolder(parent)
            TYPE_TRANSACTION -> createTransactionViewHolder(parent)
            else -> throw RuntimeException("Unknown cell type: $viewType")
        }
    }

    private fun createMonthAndYearViewHolder(parent: ViewGroup): MonthAndYearViewHolder =
        MonthAndYearViewHolder(CellTransactionMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    private fun createTransactionViewHolder(parent: ViewGroup): TransactionViewHolder =
        TransactionViewHolder(CellTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            .also { viewHolder ->
                viewHolder.itemView.setOnClickListener {
                    if (values[viewHolder.layoutPosition] is TransactionItem) {
                        val item = values[viewHolder.layoutPosition] as TransactionItem
                        onTransactionSelected.invoke(item.transaction)
                    }
                }
            }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        displayedCells.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        displayedCells.remove(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_MONTH_AND_YEAR -> (holder as MonthAndYearViewHolder)
                .rebind(item = values[position] as MonthAndYearItem)
            TYPE_TRANSACTION -> (holder as TransactionViewHolder)
                .rebind(item = values[position] as TransactionItem)
            else -> throw RuntimeException("Unknown cell type: ${holder.itemViewType}")
        }
    }

    override fun getItemViewType(position: Int): Int = values[position].type

    override fun getItemCount(): Int = values.size

    private fun getDrawable(context: Context,
                            category: Transaction.Category): Drawable? =
        getDrawable(context, category.icon, category.color)

    private fun getDrawable(context: Context,
                            drawableId: Int,
                            tintColor: Int): Drawable? {
        if (drawables.contains(drawableId)) {
            return drawables[drawableId]
        }
        return ContextCompat.getDrawable(context, drawableId)?.also {
            DrawableCompat.setTint(it, tintColor)
            drawables[drawableId] = it
        }
    }

    inner class MonthAndYearViewHolder(val binding: CellTransactionMonthBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun rebind(item: MonthAndYearItem) {
            binding.textviewMonth.text = item.monthAndYear
        }
    }

    inner class TransactionViewHolder(val binding: CellTransactionBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun rebind(item: TransactionItem) {
            binding.root.context.let { context ->
                binding.imageviewTransactionLogo.setImageDrawable(
                    getDrawable(context, item.transaction.category))
                binding.textviewTransactionName.text = item.transaction.title
                binding.textviewTransactionDate.text = item.transaction.subtitle(context)
                binding.textviewTransactionAmount.text =
                    context.getString(io.cobrowse.sample.core.R.string.transaction_amount, item.transaction.amount)
            }

        }
    }

    open class ListItem(val type: Int) {
        companion object {
            const val TYPE_MONTH_AND_YEAR = 0
            const val TYPE_TRANSACTION = 1
        }
    }

    class MonthAndYearItem(val monthAndYear: String) : ListItem(TYPE_MONTH_AND_YEAR)

    class TransactionItem(var transaction: Transaction) : ListItem(TYPE_TRANSACTION)

    companion object {
        fun from(items: List<Transaction>): TransactionsRecyclerViewAdapter {
            val groupedMapMap: Map<LocalDate, List<Transaction>> = items.groupBy {
                it.date.withDayOfMonth(1).toLocalDate()
            }

            val consolidatedList = mutableListOf<ListItem>()
            groupedMapMap.keys
                .sortedDescending()
                .forEach { next: LocalDate ->
                    consolidatedList.add(MonthAndYearItem(next.transactionGroupHeader()))
                    groupedMapMap[next]
                        ?.sortedByDescending { it.date }
                        ?.forEach {
                            consolidatedList.add(TransactionItem(it))
                        }
                }

            return TransactionsRecyclerViewAdapter(consolidatedList)
        }
    }

    override fun redactedViews(): MutableList<View> = displayedCells
        .filter { it.itemViewType == TYPE_TRANSACTION }
        .flatMap { with(it as TransactionViewHolder) {
            listOf<View>(
                this.binding.textviewTransactionName,
                this.binding.textviewTransactionDate,
                this.binding.textviewTransactionAmount)
        }}
        .toMutableList()
}