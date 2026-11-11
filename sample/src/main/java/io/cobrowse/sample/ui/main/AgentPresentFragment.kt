package io.cobrowse.sample.ui.main

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import io.cobrowse.Session
import io.cobrowse.sample.core.R
import io.cobrowse.sample.databinding.FragmentAgentPresentBinding
import io.cobrowse.sample.ui.CobrowseViewModelFactory
import io.cobrowse.sample.ui.afterTextChanged
import io.cobrowse.sample.ui.isTextEmpty

/**
 * Fragment that implements Cobrowse.io Agent Present mode: a special mode when an agent
 * can use this app to share their screen within a "reverse" screensharing session.
 */
class AgentPresentFragment : Fragment()  {

    private lateinit var viewModel: AgentPresentViewModel
    private lateinit var binding: FragmentAgentPresentBinding
    private var inputs: List<EditText> = emptyList()

    private val inputsLayout: LinearLayout
        get() = binding.agentPresentCodeLayout

    private val mainLabel: TextView
        get() = binding.logoPresentationDescription

    private val logo: ImageView
        get() = binding.logoPresentation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, CobrowseViewModelFactory())
            .get(AgentPresentViewModel::class.java)

        viewModel.cobrowseDelegate.current.observe(this@AgentPresentFragment, Observer {
            updateUiWithSession(it)
        })

        viewModel.agentSessionResult.observe(this@AgentPresentFragment, Observer {
            updateUiWithAgentSession(it.first, it.second)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgentPresentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layout = binding.agentPresentCodeLayout
        inputs = (0 ..< layout.childCount).map { i ->
            val editText = (layout.getChildAt(i) as TextInputLayout).editText
                ?: throw RuntimeException("Cannot find EditText in TextInputLayout")
            editText.afterTextChanged {
                if (it.isEmpty()) {
                    if (i > 0) {
                        inputs[i - 1].requestFocus()
                    }
                } else if (i == 5) {
                    initiateSession()
                } else {
                    inputs[i + 1].requestFocus()
                }
            }
            editText.setOnKeyListener(object : OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_DEL && editText.isTextEmpty() && i > 0) {
                        inputs[i - 1].requestFocus()
                        return true
                    }
                    return false
                }
            })
            editText
        }
        inputs[0].requestFocus()
    }

    private fun updateUiWithSession(session: Session?) {
        val isActive = session?.isActive == true
        inputsLayout.visibility = if (isActive) View.GONE else View.VISIBLE

        if (session == null || isActive) {
            resetInputs()
        }

        if ((session == null || !isActive) && inputs.isNotEmpty()) {
            inputs[0].requestFocus()
        }

        mainLabel.text = if (isActive) getString(R.string.agent_present_welcome_you_are_presenting)
                         else getString(R.string.agent_present_welcome)

        logo.visibility = if (isActive) VISIBLE else View.GONE
    }

    private fun updateUiWithAgentSession(error: Error?, session: Session?) {
        if (error == null) {
            // Presenting mode has been activated successfully
            session?.setCapabilities(arrayOfNulls(0), null)
            return
        }

        val shake = AnimationUtils.loadAnimation(this.context, io.cobrowse.sample.R.anim.shake)
        shake.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) { }
            override fun onAnimationRepeat(animation: Animation?) { }
            override fun onAnimationEnd(animation: Animation?) {
                binding.root.post {
                    resetInputs()
                }
            }
        })
        inputsLayout.startAnimation(shake)
    }

    private fun initiateSession() {
        val code = arrayOfNulls<String>(6)
        for (i in inputs.indices) {
            val typed = inputs[i].text
            if (typed.isNullOrEmpty()) {
                return
            }
            code[i] = typed[0].toString()
        }

        inputs.forEach { it.isEnabled = false }
        viewModel.initiateSession(code.joinToString(""))
    }

    private fun resetInputs() {
        if (inputs.isEmpty()) return

        inputs.forEach {
            it.text = null
            it.isEnabled = true
        }
        inputs[0].requestFocus()
    }
}