package io.cobrowse.sample.data

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.cobrowse.CobrowseIO
import io.cobrowse.Session

/**
 * Global delegate of Cobrowse.io sessions. Stores the current session and provides a way
 * to subscribe to session data updates.
 */
object CobrowseSessionDelegate
    : CobrowseIO.Delegate,
    CobrowseIO.RedactionDelegate,
    CobrowseIO.SessionMetricsDelegate{

    private val _current = MutableLiveData<Session?>()
    val current: LiveData<Session?> = _current

    private val _currentLatency = MutableLiveData<Float>(0f)
    /** Latency of the current session in milliseconds. */
    @Suppress("unused")
    val currentLatency: LiveData<Float> = _currentLatency

    override fun sessionDidUpdate(session: Session) {
        _current.value = session
    }

    override fun sessionDidEnd(session: Session) {
        _current.value = null
        _currentLatency.value = 0f
    }

    override fun sessionMetricsDidUpdate(session: Session) {
        _currentLatency.value = session.metrics().latency()
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