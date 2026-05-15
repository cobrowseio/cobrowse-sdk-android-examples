package io.cobrowse.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.cobrowse.CobrowseIO
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RedactionByDefaultDisappearingFragmentTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = PreferenceManager.getDefaultSharedPreferences(context)

    @Before
    fun enableRedactionByDefault() {
        prefs.edit().putBoolean("isRedactionByDefaultEnabled", true).commit()
    }

    @After
    fun disableRedactionByDefault() {
        prefs.edit().remove("isRedactionByDefaultEnabled").commit()
    }

    @Test
    fun whenAccountFragmentIsDisappearing_collectRedactedViewsInFragments_includesItsRootView() {
        ActivityScenario.launch(TestRedactionByDefaultActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.fragmentContainer
                val accountFragment = TestAccountFragment()

                activity.supportFragmentManager.beginTransaction()
                    .add(container.id, accountFragment, "account")
                    .commitNow()

                val accountView = accountFragment.requireView()

                assertThat(accountView.parent).isEqualTo(container)
                assertThat(container.indexOfChild(accountView)).isNotEqualTo(-1)
                assertThat(activity.collectRedactedViewsInFragments()).contains(accountView)

                container.startViewTransition(accountView)
                container.removeView(accountView)

                try {
                    assertThat(accountView.parent).isEqualTo(container)
                    assertThat(container.indexOfChild(accountView)).isEqualTo(-1)

                    assertThat(activity.collectRedactedViewsInFragments()).contains(accountView)
                } finally {
                    container.endViewTransition(accountView)
                }
            }
        }
    }
}

class TestAccountFragment : Fragment(), CobrowseIO.Redacted {

    lateinit var accountEmail: TextView
    lateinit var accountName: TextView
    lateinit var logOut: Button
    lateinit var privacyPolicy: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        accountName = TextView(requireContext()).also { addView(it) }
        accountEmail = TextView(requireContext()).also { addView(it) }
        logOut = Button(requireContext()).apply { text = "Log out" }.also { addView(it) }
        privacyPolicy = Button(requireContext()).apply { text = "Privacy Policy" }
            .also { addView(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewCreated(this)
    }

    override fun onDestroyView() {
        (activity as? ICobrowseRedactionContainer)?.notifyFragmentViewDestroyed(this)
        super.onDestroyView()
    }

    override fun redactedViews(): MutableList<View> =
        mutableListOf(accountEmail, accountName)
}
