package io.cobrowse.sample.data.model

import android.content.Context
import android.graphics.Color
import android.icu.text.MessageFormat
import android.net.Uri
import android.os.Build
import io.cobrowse.sample.core.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Data class that represents a single user financial transaction.
 */
data class Transaction(
    val title: String,
    val amount: Double,
    val date: LocalDateTime,
    val category: Category) {

    enum class Category(
        val title: String,
        val amountRange: IntRange,
        val color: Int,
        val icon: Int) {

        Childcare("Childcare", (80..200), Color.rgb(82, 161, 136), R.drawable.ic_child),
        Groceries("Groceries", (10..200), Color.rgb(82, 135, 161), R.drawable.ic_shopping_card),
        Leisure("Leisure", (3..20), Color.rgb(92, 82, 161), R.drawable.ic_movie),
        Utilities("Utilities", (60..200), Color.rgb(150, 161, 82), R.drawable.ic_home_paper);

        fun randomName (): String {
            return when (this) {
                Childcare -> listOf("Bright Horizons", "KinderCare", "Tutor Time", "Busy Bees").random()
                Groceries -> listOf("Tesco", "Walmart", "Kroger", "Sainsbury's", "Asda", "Morrisons").random()
                Leisure -> listOf("Netflix", "Amazon Prime Video", "Hulu", "Now TV", "Sky", "Disney+").random()
                Utilities -> listOf("British Gas", "EDF Energy", "NPower", "EON", "SSE", "Verizon", "AT&T", "Comcast").random()
            }
        }
    }
}

fun Transaction.subtitle(context: Context): String {
    return context.getString(R.string.transaction_detail_subtitle,
        date.dayOfMonth.getOrdinal(context),
        DateTimeFormatter.ofPattern("HH:mm").format(date))
}

fun Transaction.detailedSubtitle(context: Context): String {
    return context.getString(R.string.transaction_detail_subtitle_alt,
        DateTimeFormatter.ofPattern("MMMM").format(date),
        date.dayOfMonth.getOrdinal(context),
        DateTimeFormatter.ofPattern("y").format(date),
        DateTimeFormatter.ofPattern("HH:mm").format(date))
}

fun Int.getOrdinal(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
        formatter.format(arrayOf(this))
    } else {
        this.toString()
    }
}

/**
 * Returns a remote URL with the details of the provided transaction.
 */
fun Transaction.detailsUrl(context: Context): String =
    Uri.parse("https://cobrowseio.github.io/cobrowse-sdk-ios-examples")
        .buildUpon()
        .appendQueryParameter("title", this.title)
        .appendQueryParameter("subtitle", this.detailedSubtitle(context))
        .appendQueryParameter("amount", context.getString(R.string.transaction_amount, this.amount))
        .appendQueryParameter("category", this.category.title.lowercase())
        .build()
        .toString()

fun LocalDate.transactionGroupHeader() : String {
    return DateTimeFormatter.ofPattern("MMMM y").format(this)
}