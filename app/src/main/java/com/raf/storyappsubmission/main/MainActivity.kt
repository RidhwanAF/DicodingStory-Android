package com.raf.storyappsubmission.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.raf.storyappsubmission.R
import com.raf.storyappsubmission.adapter.LoadingStateAdapter
import com.raf.storyappsubmission.adapter.StoriesAdapter
import com.raf.storyappsubmission.databinding.ActivityMainBinding
import com.raf.storyappsubmission.maps.MapsActivity
import com.raf.storyappsubmission.model.UserPreference
import com.raf.storyappsubmission.model.ViewModelFactory
import com.raf.storyappsubmission.newstory.NewStoryActivity
import com.raf.storyappsubmission.setting.SettingActivity
import com.raf.storyappsubmission.setting.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lat: String? = null
    private var lon: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewModel(applicationContext)
        getStories()
        getCurrentLocation()
        setupAction()
    }

    private fun getCurrentLocation() {
        if(checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, getString(R.string.cannot_find_location), Toast.LENGTH_SHORT).show()
                    } else {
                        lat = ""+location.latitude.toString()
                        lon = ""+location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.location_disabled), Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermission() : Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, getString(R.string.granted_permission), Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, getString(R.string.denied_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getStories() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
        val adapter = StoriesAdapter()
        binding.rvStories.adapter = adapter

        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        mainViewModel.getUser().observe(this) { user ->
            mainViewModel.storyList(user.token).observe(this) {
                adapter.submitData(lifecycle, it)
            }
        }
    }

    private fun setupViewModel(context: Context) {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), context)
        )[MainViewModel::class.java]

        this.title = getString(R.string.title_name)

        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                binding.username.text = user.name
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private fun setupAction() {
        val fab: View = binding.fabAddStory
        fab.setOnClickListener {
            mainViewModel.getUser().observe(this) { user ->
                val token = user.token
                val intent = Intent(this, NewStoryActivity::class.java)
                intent.putExtra(NewStoryActivity.EXTRA_TOKEN, token)
                intent.putExtra(NewStoryActivity.EXTRA_LAT, lat)
                intent.putExtra(NewStoryActivity.EXTRA_LON, lon)
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity).toBundle())
            }
        }

        binding.mapButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.open_maps), Toast.LENGTH_SHORT).show()
            mainViewModel.getUser().observe(this) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra(MapsActivity.EXTRA_LAT, lat)
                intent.putExtra(MapsActivity.EXTRA_LON, lon)
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity).toBundle())
            }
        }

        binding.logoutButton.setOnClickListener {
            mainViewModel.
            logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                val i = Intent(this, SettingActivity::class.java)
                startActivity(i)
                true
            }
            else -> true
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }
}