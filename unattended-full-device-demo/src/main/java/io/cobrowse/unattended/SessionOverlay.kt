package io.cobrowse.unattended

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

/**
 * A small badge drawn over every app, so the user can see and act on a session even
 * while this app is in the background or the agent is in another app. It shows a
 * consent countdown while a session is authorizing and a "session active" badge with
 * an end button once it is running. Needs the draw-over-apps permission.
 */
class SessionOverlay(private val context: Context) {

    private enum class Mode { Consent, Active }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var view: View? = null
    private var mode: Mode? = null

    val hasPermission: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    /** Counts down from [seconds] and calls [onAccept] unless the user taps Decline first. */
    fun showConsent(seconds: Int, onAccept: () -> Unit, onDecline: () -> Unit) {
        if (mode == Mode.Consent) return
        val view = show(Mode.Consent, context.getString(R.string.decline), onDecline) ?: return
        val message = view.findViewById<TextView>(R.id.overlay_message)

        var remaining = seconds
        val tick = object : Runnable {
            override fun run() {
                if (remaining <= 0) {
                    onAccept()
                    return
                }
                message.text = context.getString(R.string.session_consent_countdown, remaining)
                remaining--
                handler.postDelayed(this, 1000)
            }
        }
        tick.run()
    }

    fun showActive(onEndSession: () -> Unit) {
        if (mode == Mode.Active) return
        val view = show(Mode.Active, context.getString(R.string.end_session), onEndSession) ?: return
        view.findViewById<TextView>(R.id.overlay_message).setText(R.string.session_active)
    }

    fun hide() {
        handler.removeCallbacksAndMessages(null)
        view?.let { windowManager.removeView(it) }
        view = null
        mode = null
    }

    private fun show(newMode: Mode, buttonLabel: String, onButton: () -> Unit): View? {
        hide()
        if (!hasPermission) return null

        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            // Touches inside the badge reach its button; everything else passes through.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (16 * context.resources.displayMetrics.density).toInt()
            // Android blocks touches that pass through overlays more opaque than 80%.
            alpha = 0.8f
        }

        return LayoutInflater.from(context).inflate(R.layout.session_overlay, null).also {
            it.findViewById<Button>(R.id.overlay_button).apply {
                text = buttonLabel
                setOnClickListener { onButton() }
            }
            windowManager.addView(it, params)
            view = it
            mode = newMode
        }
    }
}
