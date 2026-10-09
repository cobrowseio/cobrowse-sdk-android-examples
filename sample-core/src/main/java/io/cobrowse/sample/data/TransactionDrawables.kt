package io.cobrowse.sample.data

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.cobrowse.sample.data.model.Transaction

class TransactionDrawables {
    /**
     * A simple and naive drawable cache.
     */
    private val drawables = HashMap<Int, Drawable?>()

    fun getDrawable(context: Context,
                    category: Transaction.Category): Drawable? =
        getDrawable(context, category.icon, category.color)

    fun getDrawable(context: Context,
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
}