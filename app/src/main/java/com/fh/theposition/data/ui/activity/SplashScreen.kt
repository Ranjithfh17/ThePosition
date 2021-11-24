package com.fh.theposition.data.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.fh.theposition.R
import com.fh.theposition.databinding.ActivitySplashScreenBinding
import com.fh.theposition.viewmodels.MainPrefViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    private lateinit var binding:ActivitySplashScreenBinding
    private val viewModel by viewModels<MainPrefViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.launcher_icon))



        lifecycleScope.launchWhenStarted {
            delay(2000)
            viewModel.isUserLoggedIn("isFirstTime").collect { loggedIn ->
                if (loggedIn){
                    startActivity(Intent(this@SplashScreen,MainActivity::class.java))
                    this@SplashScreen.finish()
                }else{
                    startActivity(Intent(this@SplashScreen,WelcomeActivity::class.java))
                    this@SplashScreen.finish()

                }
            }
        }
    }
}