package com.raf.storyappsubmission.signin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raf.storyappsubmission.R
import com.raf.storyappsubmission.api.ApiConfig.Companion.getApiService
import com.raf.storyappsubmission.api.UserRegisterResponse
import com.raf.storyappsubmission.databinding.ActivitySignupBinding
import com.raf.storyappsubmission.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> = _isLoading

    private val _signup = MutableLiveData<UserRegisterResponse>()
    val signup: LiveData<UserRegisterResponse> = _signup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.nameEditText.addTextChangedListener {
            binding.nameEditTextLayout.error = null
        }
        binding.emailEditText.addTextChangedListener {
            binding.emailEditTextLayout.error = null
        }
        binding.passwordEditText.addTextChangedListener {
            binding.passwordEditTextLayout.error = null
        }

        binding.signupButton.setOnClickListener {
            isLoading.observe(this){
                showLoading(it)
            }

            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            when {
                name.isEmpty() -> {
                    binding.nameEditTextLayout.error = getString(R.string.name_required)
                }
                email.isEmpty() -> {
                    binding.emailEditTextLayout.error = getString(R.string.email_required)
                }
                password.isEmpty() -> {
                    binding.passwordEditTextLayout.error = getString(R.string.password_required)
                }
                else -> {
                    _isLoading.value = true
                    val service = getApiService().registerUser(name, email, password)
                    service.enqueue(object : Callback<UserRegisterResponse> {
                        override fun onResponse(
                            call: Call<UserRegisterResponse>,
                            response: Response<UserRegisterResponse>
                        ) {
                            _isLoading.value = false
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                _signup.value = responseBody!!
                                val mMessage = signup.value?.message.toString()
                                if (!responseBody.error) {
                                    AlertDialog.Builder(this@SignupActivity).apply {
                                        setTitle("Yeah!")
                                        setMessage(mMessage)
                                        setPositiveButton("Login") { _, _ ->
                                            finish()
                                            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                        }
                                        create()
                                        show()
                                    }
                                }
                            } else {
                                Toast.makeText(this@SignupActivity, response.message(), Toast.LENGTH_SHORT).show()
                              }
                        }

                        override fun onFailure(call: Call<UserRegisterResponse>, t: Throwable) {
                            _isLoading.value = false
                            Toast.makeText(this@SignupActivity, "Failed instance Retrofit", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_Y, -50F, 30F).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1F).setDuration(500)
        val nameInput = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val emailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val passwordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1F).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, nameInput, emailInput, passwordInput, signup)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}