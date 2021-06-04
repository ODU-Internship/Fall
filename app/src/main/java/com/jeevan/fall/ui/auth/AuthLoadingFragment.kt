package com.jeevan.fall.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jeevan.fall.ui.main.MainActivity
import com.jeevan.fall.R
import com.jeevan.fall.util.toast

class AuthLoadingFragment : Fragment(R.layout.fragment_auth_loading) {
    private val authViewModel: AuthViewModel by activityViewModels()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() =
            toast("Please Wait until verification is complete")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        authViewModel.otpState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    AuthViewModel.OtpStates.VERIFY_START -> {
                        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
                        authViewModel.signInUser()
                    }
                    AuthViewModel.OtpStates.VERIFY_RUNNING -> {

                    }
                    else -> {
                        backPressedCallback.remove()
                    }
                }
            }
        }
        authViewModel.signInAttemptResult.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    AuthViewModel.SignInState.COMPLETE_NEW -> {
                        val action = AuthLoadingFragmentDirections.startDetails()
                        findNavController().navigate(action)
                    }
                    AuthViewModel.SignInState.COMPLETE_EXISTING -> {
                        val intent = MainActivity.launchHome(requireContext())
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    AuthViewModel.SignInState.FAILED -> findNavController().popBackStack()
                }
            }
        }
    }
}