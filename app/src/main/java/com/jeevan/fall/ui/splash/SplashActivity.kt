package com.jeevan.fall.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jeevan.fall.ui.main.MainActivity
import com.jeevan.fall.databinding.ActivitySplashBinding
import com.jeevan.fall.domain.auth.IsLoggedInUseCase
import com.jeevan.fall.ui.auth.AuthActivity
import com.jeevan.fall.util.Result
import com.jeevan.fall.util.toast
import dagger.hilt.android.AndroidEntryPoint
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

        lifecycleScope.launchWhenStarted {
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
}