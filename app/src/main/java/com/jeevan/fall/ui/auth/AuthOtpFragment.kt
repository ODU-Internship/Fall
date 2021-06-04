package com.jeevan.fall.ui.auth

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.jeevan.fall.R
import com.jeevan.fall.databinding.FragmentAuthOtpBinding
import com.jeevan.fall.util.toast

class AuthOtpFragment : Fragment(R.layout.fragment_auth_otp) {
    private val args: AuthOtpFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by activityViewModels()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() =
            toast("Please Wait until verification is complete")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAuthOtpBinding.bind(view)
        setupBinding(binding)
        setupObservers(binding)
    }

    private fun setupBinding(binding: FragmentAuthOtpBinding) {
        val phone = args.phone
        val tvText = binding.tvAuthOtpNumber
        val tilOtp = binding.tilAuthOtp
        val btnNext = binding.btnAuthOtpNext
        val btnRetry = binding.btnAuthOtpResend

        tvText.text = "OTP has been sent to $phone"

        tilOtp.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                sendCode(tilOtp.editText?.text.toString(), tilOtp)
            }
            true
        }
        btnNext.setOnClickListener {
            sendCode(tilOtp.editText?.text.toString(), tilOtp)
        }
        btnRetry.setOnClickListener {
            authViewModel.setOtpState(AuthViewModel.OtpStates.READY)
        }
    }

    private fun setupObservers(binding: FragmentAuthOtpBinding) {
        val btnResend = binding.btnAuthOtpResend
        val btnNext = binding.btnAuthOtpNext
        val tvCounter = binding.tvAuthOtpCounter
        val tilOtp = binding.tilAuthOtp
        val phone = args.phone

        authViewModel.countDown.observe(viewLifecycleOwner) {
            if (it == "") {
                tvCounter.text = ""
                return@observe
            }
            tvCounter.text = "00:$it"
            if (it == "1") {
                authViewModel.setOtpState(AuthViewModel.OtpStates.REFRESH_ALLOWED)
            }
        }

        authViewModel.otpState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    /* This state makes the request */
                    AuthViewModel.OtpStates.READY -> {
                        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
                        authViewModel.createCredentials(phone, requireActivity())
                        btnResend.isEnabled = false
                        btnNext.isEnabled = false
                    }
                    AuthViewModel.OtpStates.RUNNING -> {
                        btnResend.isEnabled = false
                        btnNext.isEnabled = false
                    }
                    /* This state is for when the code is sent */
                    AuthViewModel.OtpStates.CODE_SENT -> {
                        backPressedCallback.remove()
                        btnResend.isEnabled = false
                        btnNext.isEnabled = true
                    }
                    /* This state is when refresh is allowed */
                    AuthViewModel.OtpStates.REFRESH_ALLOWED -> {
                        backPressedCallback.remove()
                        btnNext.isEnabled = true
                        btnResend.isEnabled = true
                    }
                    /* Code sent and google play services verified the authenticity */
                    AuthViewModel.OtpStates.AUTO_VERIFICATION_COMPLETE -> {
                        signInWithPhoneAuthCredential()
                    }
                    /* Start verification with code */
                    AuthViewModel.OtpStates.VERIFY_START -> {
                        signInWithPhoneAuthCredential()
                    }
                    /* Errors states */
                    AuthViewModel.OtpStates.VERIFY_FAILED_INVALID_CREDENTIALS -> {
                        tilOtp.error = "The entered mobile number has invalid credentials"
                    }
                    AuthViewModel.OtpStates.VERIFY_FAILED_TOO_MANY_REQUESTS -> {
                        tilOtp.error = "Too many requests, Try again later!"
                    }
                    AuthViewModel.OtpStates.VERIFY_FAILED_UNKNOWN -> {
                        tilOtp.error = "An error occurred due to unknown reasons"
                    }
                    else -> {
                        backPressedCallback.remove()
                        btnResend.isEnabled = true
                        btnNext.isEnabled = true
                    }
                }
            }
        }
    }

    private fun sendCode(code: String, tilAuthOtp: TextInputLayout) {
        if (code.length != 6) {
            tilAuthOtp.error = "Invalid OTP, OTP length must be 6 characters"
            return
        }
        if (authViewModel.verificationId.isEmpty()) {
            tilAuthOtp.error = "OTP not yet sent, wait until you receive an OTP"
            return
        }
        authViewModel.setupToken(code)
    }

    private fun signInWithPhoneAuthCredential() {
        val action = AuthOtpFragmentDirections.signIn()
        findNavController().navigate(action)
    }
}