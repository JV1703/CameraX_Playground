package com.example.intermediatesubmission.presentation.ui.fragment.authentication

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

open class BaseAuthFragment : Fragment() {

    protected val viewModel: AuthViewModel by viewModels()

}