package io.cobrowse.sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.R
import io.cobrowse.sample.data.getAndroidLogTag
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.data.model.detailsUrl
import io.cobrowse.sample.databinding.FragmentTransactionsBinding
import io.cobrowse.sample.ui.CobrowseViewModelFactory
import io.cobrowse.sample.ui.ICobrowseRedactionContainer
import io.cobrowse.sample.ui.RecyclerViewHeaderItemDecoration
import io.cobrowse.sample.ui.main.TransactionsRecyclerViewAdapter.ListItem.Companion.TYPE_MONTH_AND_YEAR

/**
 * Fragment that displays the list of transactions made in the past months.
 */
class TransactionsFragment : Fragment(), CobrowseIO.Redacted {

    @Suppress("PrivatePropertyName")
    private val Any.TAG: String
        get() = javaClass.getAndroidLogTag()

    private lateinit var viewModel: TransactionsViewModel
    private lateinit var binding: FragmentTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, CobrowseViewModelFactory())
            .get(TransactionsViewModel::class.java)
        viewModel.allTransactionsResult.observe(this@TransactionsFragment, Observer {
            updateTransactions(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewCreated(this)

        if (viewModel.allTransactionsResult.isInitialized
            && viewModel.allTransactionsResult.value?.isEmpty() == false) {
            updateTransactions(viewModel.allTransactionsResult.value!!)
        } else {
            viewModel.loadAllTransactions()
        }
    }

    override fun onDestroyView() {
        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewDestroyed(this)
        super.onDestroyView()
    }

    private fun updateTransactions(items: List<Transaction>) {
        with(binding.transactionsList) {
            val adapter = TransactionsRecyclerViewAdapter.from(items)
            adapter.setOnTransactionSelected(::onTransactionSelected)
            this.adapter = adapter
            this.addItemDecoration(RecyclerViewHeaderItemDecoration(
                this,
                isHeader = { adapter.getItemViewType(it) == TYPE_MONTH_AND_YEAR }
            ))
        }
    }

    private fun onTransactionSelected(transaction: Transaction) {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_transactionsFragment_to_transactionWebViewFragment,
                Bundle().also {
                    it.putString("url", transaction.detailsUrl(requireContext()))
                })
    }

    override fun redactedViews(): MutableList<View> {
        val redacted = mutableListOf<View>()
        binding.transactionsList.adapter.let {
            if (it is TransactionsRecyclerViewAdapter) {
                redacted.addAll(it.redactedViews())
            }
        }
        return redacted
    }
}