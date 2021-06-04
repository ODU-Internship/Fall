package com.jeevan.fall.ui.auth

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jeevan.fall.R
import com.jeevan.fall.databinding.FragmentAuthPhoneBinding

class AuthPhoneFragment : Fragment(R.layout.fragment_auth_phone) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthPhoneBinding.bind(view)
        setupBinding(binding)
    }

    private fun setupBinding(binding: FragmentAuthPhoneBinding) {
        val tilMobileNumber = binding.tilAuthMobileNumber
        val btnNext = binding.btnAuthPhoneNext

        tilMobileNumber.editText?.doOnTextChanged { _, _, _, _ ->
            tilMobileNumber.error = null
        }
        tilMobileNumber.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (!PhoneNumberUtils.isGlobalPhoneNumber(tilMobileNumber.editText?.text.toString())) {
                    tilMobileNumber.error = "The mobile number entered is invalid."
                    return@setOnEditorActionListener true
                }
                verifyNumber(tilMobileNumber.editText?.text.toString())
            }
            true
        }
        btnNext.setOnClickListener {
            if (!PhoneNumberUtils.isGlobalPhoneNumber(tilMobileNumber.editText?.text.toString())) {
                tilMobileNumber.error = "The mobile number entered is invalid."
                return@setOnClickListener
            }
            verifyNumber(tilMobileNumber.editText?.text.toString())
        }
    }

    private fun verifyNumber(phone: String) {
        val action = AuthPhoneFragmentDirections.verifyPhone(phone)
        authViewModel.phone = phone
        findNavController().navigate(action)
    }
    
}