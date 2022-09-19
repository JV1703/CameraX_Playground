package com.example.intermediatesubmission.presentation.ui.fragment.authentication

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
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseAuthFragment() {


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.authToken.observe(viewLifecycleOwner) { authToken ->
            Log.i("auth", "token: $authToken")
            if (authToken.isNotEmpty()) {
                val action = LoginFragmentDirections.actionLoginFragmentToStoryActivity()
                findNavController().navigate(action)
                activity?.finish()
            }
        }
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStringNavigation("Not registered yet? Create an Account", 20, 37)

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
                    val action = LoginFragmentDirections.actionLoginFragmentToStoryActivity()
                    findNavController().navigate(action)
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

    private fun setupStringNavigation(hyperlink: String, start: Int, end: Int) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.teal_200)
                ds.isUnderlineText = true
            }
        }
        SpannableString(hyperlink).apply {
            setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.signUpTv.text = this
        }
        binding.signUpTv.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun login(email: String, password: String) {
        viewModel.login(email, password)
    }

    private fun validator(email: String, password: String) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            makeToast("please check your email field")
        } else if (password.isEmpty() || password.length < 5) {
            makeToast("please check your password field")
        } else {
            login(email, password)
        }
        Log.i("login_fragment", "email: $email, password: $password")
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