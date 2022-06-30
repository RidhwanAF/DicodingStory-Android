package com.raf.storyappsubmission.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.raf.storyappsubmission.R
import com.raf.storyappsubmission.databinding.ActivitySettingBinding
import com.raf.storyappsubmission.model.UserPreference
import com.raf.storyappsubmission.model.ViewModelFactory

class SettingActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.setting)
        supportActionBar?.hide()

        setTheme(applicationContext)
        setupAction()
    }

    private fun setTheme(context: Context) {
        val switchTheme = binding.switchTheme
        val pref = UserPreference.getInstance(dataStore)
        val mainViewModel = ViewModelProvider(this,
            ViewModelFactory(pref, context))[SettingViewModel::class.java]
        mainViewModel.getThemeSettings().observe(this
        ) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                switchTheme.isChecked = false
            }
        }

        switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mainViewModel.saveThemeSetting(isChecked)
        }
    }

    private fun setupAction(){
        binding.cvLanguage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }
}