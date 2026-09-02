package io.cobrowse.sample.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.cobrowse.Session
import io.cobrowse.sample.R
import io.cobrowse.sample.data.CobrowseSessionDelegate
import io.cobrowse.sample.databinding.DialogConsentBinding

class ConsentDialogFragment : DialogFragment() {

    enum class Request(
        @StringRes val heading: Int,
        @StringRes val body: Int,
        @StringRes val terms: Int
    ) {
        Screenshare(
            R.string.consent_session_heading,
            R.string.consent_session_body,
            R.string.consent_session_terms
        ),
        RemoteControl(
            R.string.consent_remote_control_heading,
            R.string.consent_remote_control_body,
            R.string.consent_remote_control_terms
        ),
        FullDevice(
            R.string.consent_full_device_heading,
            R.string.consent_full_device_body,
            R.string.consent_full_device_terms
        );

        fun isPending(session: Session) = when (this) {
            // The SDK asks for consent while the session is authorizing, not pending
            Screenshare -> session.isAuthorizing
            RemoteControl -> session.remoteControl() == Session.RemoteControlState.Requested
            FullDevice -> session.fullDevice() == Session.FullDeviceState.Requested
        }

        fun allow(session: Session) = when (this) {
            Screenshare -> session.activate(null)
            RemoteControl -> session.setRemoteControl(Session.RemoteControlState.On, null)
            FullDevice -> session.setFullDevice(Session.FullDeviceState.On, null)
        }

        fun deny(session: Session) = when (this) {
            Screenshare -> session.end(null)
            RemoteControl -> session.setRemoteControl(Session.RemoteControlState.Rejected, null)
            FullDevice -> session.setFullDevice(Session.FullDeviceState.Rejected, null)
        }
    }

    private var _binding: DialogConsentBinding? = null
    private val binding get() = _binding!!

    private val request: Request
        get() = Request.valueOf(requireNotNull(requireArguments().getString(ARGUMENT_REQUEST)))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConsentBinding.inflate(inflater, container, false)
        isCancelable = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.consentHeading.setText(request.heading)
        binding.consentBody.setText(request.body)
        binding.consentTerms.setText(request.terms)

        binding.consentDeny.setOnClickListener { answer(allowed = false) }
        binding.consentAllow.setOnClickListener { answer(allowed = true) }

        CobrowseSessionDelegate.current.observe(viewLifecycleOwner) { session ->
            if (session == null || !request.isPending(session)) {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val width = resources.getDimensionPixelSize(R.dimen.consent_card_width)
        val margin = resources.getDimensionPixelSize(R.dimen.consent_card_margin)
        val available = resources.displayMetrics.widthPixels - 2 * margin

        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(minOf(width, available), WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun answer(allowed: Boolean) {
        CobrowseSessionDelegate.current.value?.let {
            if (allowed) request.allow(it) else request.deny(it)
        }

        dismiss()
    }

    companion object {

        val TAG: String = ConsentDialogFragment::class.java.simpleName

        private const val ARGUMENT_REQUEST = "request"

        fun show(request: Request, manager: FragmentManager) {
            if (manager.findFragmentByTag(TAG) != null) return

            ConsentDialogFragment()
                .apply { arguments = bundleOf(ARGUMENT_REQUEST to request.name) }
                .show(manager, TAG)
        }
    }
}
