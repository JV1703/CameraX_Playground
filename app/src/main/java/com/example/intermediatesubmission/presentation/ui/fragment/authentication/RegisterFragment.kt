package com.example.intermediatesubmission.presentation.ui.fragment.authentication

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseAuthFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signUpBtn.setOnClickListener {
            validator(
                binding.edRegisterEmail.text.toString(),
                binding.edRegisterName.text.toString(),
                binding.edRegisterPassword.text.toString()
            )
        }

        viewModel.registerResponse.observe(viewLifecycleOwner) { networkResult ->
            when (networkResult) {
                is NetworkResult.Loading -> {
                    isLoading(true)
                }
                is NetworkResult.Success -> {
                    isLoading(false)
                    login(
                        binding.edRegisterEmail.text.toString(),
                        binding.edRegisterPassword.text.toString()
                    )
                    makeToast(getString(R.string.register_success))
                    val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
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

    private fun isLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.signUpBtn.isEnabled = false
            binding.progressInd.visibility = View.VISIBLE
        } else {
            binding.signUpBtn.isEnabled = true
            binding.progressInd.visibility = View.GONE
        }
    }

    private fun validator(email: String, username: String, password: String) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            makeToast(getString(R.string.check_email))
        } else if (username.isEmpty()) {
            makeToast(getString(R.string.check_username))
        } else if (password.isEmpty() || password.length < 5) {
            makeToast(getString(R.string.check_email))
        } else {
            registerUser(username, email, password)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        viewModel.registerUser(name, email, password)
    }

    private fun login(email: String, password: String) {
        viewModel.login(email, password)
    }

}