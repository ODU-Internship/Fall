package com.jeevan.fall.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jeevan.fall.databinding.ActivityMainBinding
import com.jeevan.fall.util.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.pow
import kotlin.math.sqrt


@AndroidEntryPoint
class   MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupBinding()
        setupObservers()
    }

    private fun setupBinding() {

    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        mainViewModel.accelerometerData.observe(this) {
            it?.let {
                when (it) {
                    is Result.Success -> {
                        val (x, y, z) = it.data
                        binding.lpiMainAcclXAxis.setProgressCompat(50 + x.toInt(), true)
                        binding.lpiMainAcclYAxis.setProgressCompat(50 + y.toInt(), true)
                        binding.lpiMainAcclZAxis.setProgressCompat(50 + z.toInt(), true)
                        binding.lpiMainAcclMagnitude.setProgressCompat(
                            sqrt(x.pow(2) + y.pow(2) + z.pow(2)).toInt(),
                            true
                        )
                    }
                    else -> {

                    }
                }
            }
        }
        mainViewModel.gyroscopeData.observe(this) {
            it?.let {
                when (it) {
                    is Result.Success -> {
                        val (x, y, z) = it.data
                        binding.lpiMainGyroXAxis.setProgressCompat(50 + x.toInt(), true)
                        binding.lpiMainGyroYAxis.setProgressCompat(50 + y.toInt(), true)
                        binding.lpiMainGyroZAxis.setProgressCompat(50 + z.toInt(), true)
                        binding.lpiMainGyroMagnitude.setProgressCompat(
                            sqrt(x.pow(2) + y.pow(2) + z.pow(2)).toInt(),
                            true
                        )
                    }
                    else -> {

                    }
                }
            }
        }
        mainViewModel.rotationVector.observe(this) {
            it?.let {
                when (it) {
                    is Result.Success -> {
                        val (x, y, z) = it.data
                        binding.tvRotX.text = "X:Axis: %.2f".format(x)
                        binding.tvRotY.text = "Y:Axis: %.2f".format(y)
                        binding.tvRotZ.text = "Z:Axis: %.2f".format(z)
                    }
                    else -> {

                    }
                }
            }
        }
        mainViewModel.isAFall.observe(this) {
            it?.let {
                when (it) {
                    is Result.Success -> {
                       if(it.data) {
                           binding.tvFallAlert.visibility = View.VISIBLE
                           //sending message goes here!
                       }
                    }
                    else -> {

                    }
                }
            }
        }
    }


    companion object {
        fun launchHome(context: Context) = Intent(context, MainActivity::class.java)
    }
}