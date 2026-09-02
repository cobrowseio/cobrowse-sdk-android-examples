package io.cobrowse.sample.data

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.cobrowse.CobrowseIO
import io.cobrowse.Session
import io.cobrowse.sample.ui.ConsentDialogFragment

/**
 * Global delegate of Cobrowse.io sessions. Stores the current session and provides a way
 * to subscribe to session data updates.
 */
object CobrowseSessionDelegate : CobrowseIO.Delegate, CobrowseIO.RedactionDelegate,
    CobrowseIO.SessionRequestDelegate, CobrowseIO.RemoteControlRequestDelegate,
    CobrowseIO.FullDeviceRequestDelegate {

    private val _current = MutableLiveData<Session?>()
    val current: LiveData<Session?> = _current

    override fun sessionDidUpdate(session: Session) {
        _current.value = session
    }

    override fun sessionDidEnd(session: Session) {
        _current.value = null
    }

    override fun handleSessionRequest(activity: Activity?, session: Session) {
        _current.value = session

        showConsent(ConsentDialogFragment.Request.Screenshare, activity)
    }

    override fun handleRemoteControlRequest(activity: Activity?, session: Session) {
        showConsent(ConsentDialogFragment.Request.RemoteControl, activity)
    }

    override fun handleFullDeviceRequest(activity: Activity?, session: Session) {
        showConsent(ConsentDialogFragment.Request.FullDevice, activity)
    }

    private fun showConsent(request: ConsentDialogFragment.Request, activity: Activity?) {
        val manager = (activity as? FragmentActivity)?.supportFragmentManager ?: return

        ConsentDialogFragment.show(request, manager)
    }

    override fun redactedViews(activity: Activity): MutableList<View> {
        return if (isRedactionByDefaultEnabled(activity))
            mutableListOf(activity.window.decorView)
            else mutableListOf()
    }

    /**
     * Returns a value indicating whether UI components should be redacted by default or not.
     */
    fun isRedactionByDefaultEnabled(context: Context)
        = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean("isRedactionByDefaultEnabled", false)

    /**
     * Helper function to get the delegate object in a more obvious way.
     */
    fun getInstance(): CobrowseSessionDelegate {
        return this
    }
}