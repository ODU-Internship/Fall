package com.jeevan.fall.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jeevan.fall.data.sensor.models.FallLevel
import com.jeevan.fall.databinding.ActivityMainBinding
import com.jeevan.fall.util.Result
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.sqrt


@AndroidEntryPoint
class   MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by viewModels()
    private val shouldSend = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupBinding()
        setupObservers()
    }

    private fun setupBinding() {
        binding.tvGyroHeading.setOnClickListener {
            binding.tvFallAlert.visibility = View.INVISIBLE
            binding.tvFallCaution.visibility = View.INVISIBLE
            shouldSend.set(true)
        }

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
                        when (it.data) {
                            FallLevel.Change -> {
                                if(binding.tvFallAlert.visibility == View.VISIBLE) {
                                    return@let
                                }
                                binding.tvFallAlert.visibility = View.GONE
                                binding.tvFallCaution.visibility = View.VISIBLE
                                //sending message goes here!
                                sendMessage("+918073298546", "Change in position for Patient #2")
                            }
                            FallLevel.Fall -> {
                                binding.tvFallAlert.visibility = View.VISIBLE
                                binding.tvFallCaution.visibility = View.GONE
                                // sending message goes here!
                                sendMessage("+918073298546", "fall alert for Patient #2")
                                }
                            FallLevel.None -> {

                            }

                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun sendMessage(mobile: String, message: String) {
        if(shouldSend.compareAndSet(true, false)) {
            val sms = SmsManager.getDefault();
            sms.sendTextMessage(mobile, null,message, null, null);
        }
    }


    companion object {
        fun launchHome(context: Context) = Intent(context, MainActivity::class.java)
    }
}