package com.jeevan.fall.ui.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jeevan.fall.databinding.ActivitySplashBinding
import com.jeevan.fall.domain.auth.IsLoggedInUseCase
import com.jeevan.fall.ui.auth.AuthActivity
import com.jeevan.fall.ui.main.MainActivity
import com.jeevan.fall.util.Result
import com.jeevan.fall.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var isLoggedIn: IsLoggedInUseCase
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        launchHome()
    }

    fun tryLogin() {
        lifecycleScope.launch {
            isLoggedIn(Unit).let {
                when (it) {
                    is Result.Success -> {
                        if (it.data) {
                            val intent = MainActivity.launchHome(this@SplashActivity)
                            startActivity(intent)
                        } else {
                            val intent = AuthActivity.launchAuth(this@SplashActivity)
                            startActivity(intent)
                        }
                        finish()
                    }
                    else -> {
                        toast("There seems to be an error, try again later")
                    }
                }
            }
        }
    }

    fun launchHome() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    tryLogin()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    finish()
                }

            }
        when (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                tryLogin()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.SEND_SMS
                )
            }
        }
    }

}