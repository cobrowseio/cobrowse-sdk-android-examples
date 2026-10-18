package io.cobrowse.unattended

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.cobrowse.CobrowseAccessibilityService
import io.cobrowse.CobrowseIO
import io.cobrowse.Session

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // The app targets SDK 35+, so it is drawn edge-to-edge and must inset itself.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.root)) { root, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            toolbar.setPadding(0, insets.top, 0, 0)
            root.setPadding(insets.left, 0, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        status = findViewById(R.id.status)

        findViewById<Button>(R.id.end_session_button).setOnClickListener {
            CobrowseIO.instance().currentSession()?.end { err, _ -> showError(err) }
        }

        findViewById<Button>(R.id.overlay_permission_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        findViewById<Button>(R.id.accessibility_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        (application as DemoApplication).onSessionChanged = { runOnUiThread { updateUi() } }
        updateUi()
    }

    override fun onPause() {
        super.onPause()
        (application as DemoApplication).onSessionChanged = null
    }

    private fun updateUi() {
        val session = CobrowseIO.instance().currentSession()
        val active = session?.takeIf { it.isActive }
        val serviceRunning = CobrowseAccessibilityService.isRunning()
        val canDrawOverlays = SessionOverlay(this).hasPermission

        val sessionState = when {
            session == null -> "none, waiting for an agent to connect"
            session.isPending -> "pending, waiting for an agent"
            session.isAuthorizing -> "authorizing"
            session.isActive -> "active"
            session.isEnded -> "ended, waiting for the next agent"
            else -> session.state()
        }

        val fullDevice = when (active?.fullDevice()) {
            null -> "-"
            Session.FullDeviceState.Off -> "off, sharing this app only"
            Session.FullDeviceState.Requested -> "requested, waiting for approval"
            Session.FullDeviceState.Rejected -> "rejected"
            Session.FullDeviceState.On ->
                if (serviceRunning) "on, sharing the whole device"
                else "on, but the capture dialog needs a tap while the accessibility service is off"
        }

        val remoteControl = when (active?.remoteControl()) {
            null -> "-"
            Session.RemoteControlState.Off -> "off"
            Session.RemoteControlState.Requested -> "requested, waiting for approval"
            Session.RemoteControlState.Rejected -> "rejected"
            Session.RemoteControlState.On -> when {
                active.fullDevice() != Session.FullDeviceState.On -> "on, this app only"
                serviceRunning -> "on, whole device"
                else -> "on, but controlling other apps needs the accessibility service"
            }
        }

        val accessibility =
            if (serviceRunning) "running"
            else "off, enable it below to allow full-device remote control"

        status.text = listOf(
            "Device ID" to CobrowseIO.instance().deviceId(),
            "Session" to sessionState,
            "Full device" to fullDevice,
            "Remote control" to remoteControl,
            "Accessibility service" to accessibility,
            "Session overlay" to
                if (canDrawOverlays) "allowed, shown over other apps while a session is active"
                else "not allowed, enable it below to show an indicator over other apps",
        ).joinToString("\n\n") { (label, value) -> "$label\n  $value" }

        findViewById<Button>(R.id.end_session_button).visibility =
            if (active == null) View.GONE else View.VISIBLE
        findViewById<Button>(R.id.overlay_permission_button).visibility =
            if (canDrawOverlays) View.GONE else View.VISIBLE
    }

    private fun showError(err: Error?) {
        if (err != null) Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
    }
}
