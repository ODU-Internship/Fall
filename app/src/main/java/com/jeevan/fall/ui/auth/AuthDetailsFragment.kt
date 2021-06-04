package com.jeevan.fall.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.jeevan.fall.ui.main.MainActivity
import com.jeevan.fall.R
import com.jeevan.fall.databinding.FragmentAuthDetailsBinding
import com.jeevan.fall.util.Result
import java.util.concurrent.atomic.AtomicBoolean

class AuthDetailsFragment : Fragment(R.layout.fragment_auth_details) {
    private val activityViewModel: AuthViewModel by activityViewModels()
    private val btnClicked = AtomicBoolean(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthDetailsBinding.bind(view)
        setupBinding(binding)
        setupObserver(binding)
    }

    private fun setupObserver(binding: FragmentAuthDetailsBinding) {
        binding.btnAuthDetailsNext.setOnClickListener {
            if (btnClicked.compareAndSet(false, true)) {
                activityViewModel.updateDetails(
                    binding.tilAuthDetailsName.editText?.text.toString(),
                    binding.tilAuthDetailsAge.editText?.text.toString().toInt(),
                    binding.tilAuthDetailsAddress.editText?.text.toString(),
                )
            }
        }

    }

    private fun setupBinding(binding: FragmentAuthDetailsBinding) {
        activityViewModel.updateDetailsResult.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Result.Loading -> {
                        binding.btnAuthDetailsNext.isEnabled = false
                    }
                    is Result.Success -> {
                        binding.btnAuthDetailsNext.isEnabled = true
                        val intent = MainActivity.launchHome(requireContext())
                        startActivity(intent)
                        requireActivity().finish()
                        btnClicked.set(false)
                    }
                    else -> {
                        binding.btnAuthDetailsNext.isEnabled = true
                        btnClicked.set(false)
                    }
                }
            }
        }
    }
}