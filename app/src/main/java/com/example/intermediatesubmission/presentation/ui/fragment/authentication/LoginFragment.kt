package com.example.intermediatesubmission.presentation.ui.fragment.authentication

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentLoginBinding
import com.example.intermediatesubmission.presentation.ui.activity.StoryActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseAuthFragment() {


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.e("trace", "loginFragment - onCreateView: Started")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpannable(
            binding.signUpTv,
            getString(R.string.cta_register1),
            getString(R.string.cta_register2),
            R.color.teal_200
        )

        binding.signInBtn.setOnClickListener {
            validator(binding.edLoginEmail.text.toString(), binding.edLoginPassword.text.toString())
        }

        viewModel.loginResponse.observe(viewLifecycleOwner) { networkResult ->
            when (networkResult) {
                is NetworkResult.Loading -> {
                    isLoading(true)
                }
                is NetworkResult.Success -> {
                    isLoading(false)
                    val intent = Intent(requireContext(), StoryActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                is NetworkResult.Error -> {
                    isLoading(false)
                    makeToast(networkResult.message ?: "unexpected error")
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setSpannable(
        textView: TextView, initialMsg: String, highlightString: String, @ColorRes color: Int
    ) {

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), color)
                ds.isUnderlineText = true
            }
        }

        val fullText = "$initialMsg $highlightString"
        textView.text = fullText

        val startIndex = fullText.indexOf(highlightString)
        val endIndex = startIndex + highlightString.length

        SpannableString(fullText).apply {
            setSpan(
                clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = this
        }

        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun login(email: String, password: String) {
        viewModel.login(email, password)
    }

    private fun validator(email: String, password: String) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            makeToast(getString(R.string.check_email))
        } else if (password.isEmpty() || password.length < 5) {
            makeToast(getString(R.string.check_password))
        } else {
            login(email, password)
        }
    }

    private fun isLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.signInBtn.isEnabled = false
            binding.progressInd.visibility = View.VISIBLE
        } else {
            binding.signInBtn.isEnabled = true
            binding.progressInd.visibility = View.GONE
        }
    }
}