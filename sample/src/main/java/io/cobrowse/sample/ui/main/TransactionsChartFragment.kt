package io.cobrowse.sample.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.R
import io.cobrowse.sample.data.CobrowseSessionDelegate
import io.cobrowse.sample.data.getAndroidLogTag
import io.cobrowse.sample.data.model.Transaction
import io.cobrowse.sample.databinding.FragmentTransactionsChartBinding
import io.cobrowse.sample.ui.CobrowseViewModelFactory
import io.cobrowse.sample.ui.ICobrowseRedactionContainer
import io.cobrowse.sample.ui.onSizeChange

/**
 * Fragment that displays the recent transactions statistics.
 */
class TransactionsChartFragment : Fragment(), CobrowseIO.Redacted, CobrowseIO.Unredacted {

    @Suppress("PrivatePropertyName")
    private val Any.TAG: String
        get() = javaClass.getAndroidLogTag()

    private lateinit var viewModel: TransactionsChartViewModel
    private lateinit var binding: FragmentTransactionsChartBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "FCM SDK and the app can post notifications")
        } else {
            Log.w(TAG, "FCM SDK and the app will not show notifications")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, CobrowseViewModelFactory())
            .get(TransactionsChartViewModel::class.java)

        viewModel.recentTransactionsResult.observe(this@TransactionsChartFragment, Observer {
            updateChart(it)
        })
        viewModel.balanceResult.observe(this@TransactionsChartFragment, Observer {
            updateBalance(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsChartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewCreated(this)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.accountFragment -> {
                        //return menuItem.onNavDestinationSelected(findNavController(view))
                        findNavController(view).navigate(R.id.action_transactionsChartFragment_to_accountFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (viewModel.recentTransactionsResult.isInitialized
            && viewModel.recentTransactionsResult.value?.isEmpty() == false) {
            updateChart(viewModel.recentTransactionsResult.value!!)
        } else {
            viewModel.loadRecentTransactions()
        }

        if (viewModel.balanceResult.isInitialized) {
            updateBalance(viewModel.balanceResult.value)
        } else {
            viewModel.loadBalance()
        }

        binding.chart.onSizeChange {
            resizeChartSummary()
        }
        askNotificationPermission()
    }

    override fun onDestroyView() {
        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewDestroyed(this)
        super.onDestroyView()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "FCM SDK and the app can post notifications")
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateBalance(balance: Double?) {
        if (balance != null)
            binding.textviewBalance.text = getString(R.string.transaction_amount, balance)
    }

    private fun updateChart(transactions: List<Transaction>) {
        val chart: PieChart = binding.chart
        val total: TextView = binding.textviewTotalSpent

        val transactionsDictionary = transactions
            .groupBy { it.category }
            .mapValues { next -> next.value.sumOf { it.amount } }
        val pieEntries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        for (transaction in transactionsDictionary) {
            colors.add(transaction.key.color)

            val icon = getDrawable(requireContext(), transaction.key.icon)
            icon?.setTint(Color.WHITE)
            pieEntries.add(PieEntry(transaction.value.toFloat(), icon))
        }

        total.text = getString(R.string.transaction_amount, transactionsDictionary.values.sum())

        val pieDataSet = PieDataSet(pieEntries, "type")
        pieDataSet.valueTextSize = 12f
        pieDataSet.colors = colors
        pieDataSet.sliceSpace = 4f

        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(false)

        chart.description = null
        chart.legend.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(getColor(requireContext(), android.R.color.transparent))
        chart.data = pieData
        chart.invalidate()
    }

    private fun resizeChartSummary() {
        val chart: PieChart = binding.chart
        val total: TextView = binding.textviewTotalSpent
        val totalHeader: TextView = binding.textviewTotalSpentHeader
        val totalFooter: TextView = binding.textviewTotalSpentFooter
        val balance: TextView = binding.textviewBalance

        val desiredWidth = ((chart.holeRadius / 100f) * (chart.radius) * 1.5f).toInt()
        val desiredHeight = desiredWidth / 3

        if (total.layoutParams is ConstraintLayout.LayoutParams) {
            total.layoutParams.width = desiredWidth
            total.layoutParams.height = desiredHeight
            total.requestLayout()
        }
        if (totalHeader.layoutParams is ConstraintLayout.LayoutParams) {
            totalHeader.layoutParams.width = desiredWidth
            totalHeader.requestLayout()
        }
        if (totalFooter.layoutParams is ConstraintLayout.LayoutParams) {
            totalFooter.layoutParams.width = desiredWidth
            totalFooter.requestLayout()
        }
        if (balance.layoutParams is ConstraintLayout.LayoutParams) {
            // Set the balance label size the same like in the "spent this month" height,
            // so their text sizes will be the same.
            balance.layoutParams.width = desiredWidth
            balance.layoutParams.height = desiredHeight
            balance.requestLayout()
        }
    }

    override fun redactedViews(): MutableList<View> {
        return mutableListOf(
            binding.textviewBalance,
            binding.textviewTotalSpent
        )
    }

    override fun unredactedViews(): MutableList<View> {
        return if (CobrowseSessionDelegate.isRedactionByDefaultEnabled(requireContext()))
            mutableListOf(
                binding.textviewBalanceHeader,
                binding.textviewTotalSpentHeader,
                binding.textviewTotalSpent,
                binding.textviewTotalSpentFooter,
                binding.chart
            )
            else mutableListOf()
    }
}