package io.cobrowse.sample.ui

import android.view.View
import androidx.fragment.app.Fragment
import io.cobrowse.CobrowseIO

/**
 * Helps track all fragments with active (alive) views in an activity, including fragments which are
 * being removed (replaced) in an animated transactions and not accessible via
 * [androidx.fragment.app.FragmentManager] APIs.
 */
interface ICobrowseRedactionContainer {
    /**
     * Function supposed to be invoked from a fragment when its view is created.
     */
    fun notifyFragmentViewCreated(fragment: Fragment)
    /**
     * Function supposed to be invoked from a fragment when its view is destroyed.
     */
    fun notifyFragmentViewDestroyed(fragment: Fragment)
    /**
     * Finds all fragments that implement [CobrowseIO.Redacted] and collects all redacted views
     * from them.
     */
    fun collectRedactedViewsInFragments() : MutableList<View>
    /**
     * Finds all fragments that implement [CobrowseIO.Unredacted] and collects all unredacted views
     * from them.
     */
    fun collectUnredactedViewsInFragments() : MutableList<View>
}

class CobrowseRedactionContainer : ICobrowseRedactionContainer {
    private val fragmentsWithViews = mutableSetOf<Fragment>()

    override fun notifyFragmentViewCreated(fragment: Fragment) {
        fragmentsWithViews.add(fragment)
    }

    override fun notifyFragmentViewDestroyed(fragment: Fragment) {
        fragmentsWithViews.remove(fragment)
    }

    override fun collectRedactedViewsInFragments(): MutableList<View> {
        return this.fragmentsWithViews
                .filter { it is CobrowseIO.Redacted }
                .flatMap { (it as CobrowseIO.Redacted).redactedViews() }
                .filterNotNull()
                .toMutableList()
    }

    override fun collectUnredactedViewsInFragments(): MutableList<View> {
        return this.fragmentsWithViews
            .filter { it is CobrowseIO.Unredacted }
            .flatMap { (it as CobrowseIO.Unredacted).unredactedViews() }
            .filterNotNull()
            .toMutableList()
    }

}