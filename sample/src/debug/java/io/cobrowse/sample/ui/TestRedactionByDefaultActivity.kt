package io.cobrowse.sample.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity

@VisibleForTesting
class TestRedactionByDefaultActivity : AppCompatActivity(),
    ICobrowseRedactionContainer by CobrowseRedactionContainer() {

    lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        setContentView(fragmentContainer)
    }
}
