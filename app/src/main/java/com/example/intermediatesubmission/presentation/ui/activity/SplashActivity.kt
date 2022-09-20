package com.example.intermediatesubmission.presentation.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.intermediatesubmission.databinding.ActivitySplashBinding
import com.example.intermediatesubmission.presentation.ui.fragment.authentication.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.authToken.observe(this) { authToken ->
            ObjectAnimator.ofFloat(binding.logoIv, View.ALPHA, 1F).apply {
                duration = 1500
                addListener(object :
                    AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (authToken == "") {
                            val intent = Intent(this@SplashActivity, AuthActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@SplashActivity, StoryActivity::class.java)
                            startActivity(intent)
                        }
                        finish()
                    }
                })
                start()
            }
        }
    }
}