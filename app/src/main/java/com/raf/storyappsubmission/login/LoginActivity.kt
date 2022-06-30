package com.raf.storyappsubmission.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.raf.storyappsubmission.R
import com.raf.storyappsubmission.api.ApiConfig.Companion.getApiService
import com.raf.storyappsubmission.api.UserLoginResponse
import com.raf.storyappsubmission.databinding.ActivityLoginBinding
import com.raf.storyappsubmission.main.MainActivity
import com.raf.storyappsubmission.model.UserModel
import com.raf.storyappsubmission.model.UserPreference
import com.raf.storyappsubmission.model.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> = _isLoading

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var user: UserModel

    private val _login = MutableLiveData<UserLoginResponse>()
    val login: LiveData<UserLoginResponse> = _login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel(applicationContext)
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

    private fun setupViewModel(context: Context) {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), context)
        )[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this) { user ->
            this.user = user
        }
    }

    private fun setupAction(){
        binding.passwordEditText.addTextChangedListener {
            binding.passwordEditTextLayout.error = null
        }

        binding.emailEditText.addTextChangedListener {
            binding.emailEditTextLayout.error = null
        }

        binding.loginButton.setOnClickListener {
            isLoading.observe(this){
                showLoading(it)
            }

            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            when {
                email.isEmpty() -> {
                    binding.emailEditTextLayout.error = getString(R.string.input_email)
                }
                password.isEmpty() -> {
                    binding.passwordEditTextLayout.error = getString(R.string.input_password)
                }
                else -> {
                    _isLoading.value = true
                    loginViewModel.login()

                    val service = getApiService().loginUser(email, password)
                    service.enqueue(object: Callback<UserLoginResponse> {
                        override fun onResponse(
                            call: Call<UserLoginResponse>,
                            response: Response<UserLoginResponse>
                        ) {
                            _isLoading.value = false
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                _login.value = responseBody!!
                                val mMessage = login.value?.message.toString()
                                val userId = login.value?.loginResult?.userId.toString()
                                val name = login.value?.loginResult?.name.toString()
                                val token = login.value?.loginResult?.token.toString()

                                if (!responseBody.error) {
                                    loginViewModel.saveUser(UserModel(userId, name, "", "", token, true))
                                    AlertDialog.Builder(this@LoginActivity).apply {
                                        setTitle("Yeaay!")
                                        setMessage(mMessage)
                                        setPositiveButton("Go") { _, _ ->
                                            val intent = Intent(context, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                        create()
                                        show()
                                    }
                                }
                            } else {
                                when {
                                    email != user.email || password != user.password -> {
                                        Toast.makeText(this@LoginActivity, getString(R.string.invalid_email_password), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<UserLoginResponse>, t: Throwable) {
                            _isLoading.value = true
                            Toast.makeText(this@LoginActivity, "Failed instance Retrofit", Toast.LENGTH_SHORT).show()
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
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1F).setDuration(500)
        val emailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val passwordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1F).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(title, message)
        }

        AnimatorSet().apply {
            playSequentially(together, emailInput, passwordInput, login)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}