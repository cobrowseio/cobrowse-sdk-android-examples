package io.cobrowse.sample.data

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.cobrowse.CobrowseIO
import io.cobrowse.Session

/**
 * Global delegate of Cobrowse.io sessions. Stores the current session and provides a way
 * to subscribe to session data updates.
 */
object CobrowseSessionDelegate : CobrowseIO.Delegate, CobrowseIO.RedactionDelegate,
    CobrowseIO.SessionRequestDelegate, CobrowseIO.RemoteControlRequestDelegate,
    CobrowseIO.FullDeviceRequestDelegate {

    private val _current = MutableLiveData<Session?>()
    val current: LiveData<Session?> = _current

    /**
     * Shows the app's own prompt asking the user to consent to an agent's request.
     */
    var consentPrompt: (request: ConsentRequest, activity: Activity?) -> Unit = { _, _ -> }

    /**
     * Hides the whole activity window from the agent when redaction by default is enabled.
     * Compose UI turns it off and redacts its root composable instead, which lets it unredact
     * parts of the screen.
     */
    var redactWindowByDefault = true

    override fun sessionDidUpdate(session: Session) {
        _current.value = session
    }

    override fun sessionDidEnd(session: Session) {
        _current.value = null
    }

    override fun handleSessionRequest(activity: Activity?, session: Session) {
        _current.value = session

        consentPrompt(ConsentRequest.Screenshare, activity)
    }

    override fun handleRemoteControlRequest(activity: Activity?, session: Session) {
        consentPrompt(ConsentRequest.RemoteControl, activity)
    }

    override fun handleFullDeviceRequest(activity: Activity?, session: Session) {
        consentPrompt(ConsentRequest.FullDevice, activity)
    }

    override fun redactedViews(activity: Activity): MutableList<View> {
        return if (redactWindowByDefault && isRedactionByDefaultEnabled(activity))
            mutableListOf(activity.window.decorView)
            else mutableListOf()
    }

    /**
     * Returns a value indicating whether UI components should be redacted by default or not.
     */
    fun isRedactionByDefaultEnabled(context: Context)
        = context.preferences.getBoolean(KEY_REDACTION_BY_DEFAULT, false)

    /**
     * Sets whether UI components should be redacted by default or not.
     */
    fun setRedactionByDefaultEnabled(context: Context, enabled: Boolean) {
        context.preferences.edit().putBoolean(KEY_REDACTION_BY_DEFAULT, enabled).apply()
    }

    private const val KEY_REDACTION_BY_DEFAULT = "isRedactionByDefaultEnabled"

    /**
     * The default shared preferences, the same that AndroidX Preference uses.
     */
    private val Context.preferences
        get() = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

    /**
     * Helper function to get the delegate object in a more obvious way.
     */
    fun getInstance(): CobrowseSessionDelegate {
        return this
    }
}