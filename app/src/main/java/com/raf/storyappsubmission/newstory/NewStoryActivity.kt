package com.raf.storyappsubmission.newstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raf.storyappsubmission.R
import com.raf.storyappsubmission.api.ApiConfig.Companion.getApiService
import com.raf.storyappsubmission.api.FileUploadResponse
import com.raf.storyappsubmission.databinding.ActivityNewStoryBinding
import com.raf.storyappsubmission.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private lateinit var currentPhotoPath: String

    private var getFile: File? = null
    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> = _isLoading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title = getString(R.string.add_new_story)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        getCurrentLocation()
        showTabLayout()
        setupAction()
    }

    private fun getCurrentLocation() {
        val lat = intent.getStringExtra(EXTRA_LAT)
        val lon = intent.getStringExtra(EXTRA_LON)

        binding.tvLat.text = lat
        binding.tvLon.text = lon
    }

    private fun showTabLayout() {
        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home ->{
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                super.onOptionsItemSelected(item)
            }
            else -> true
        }
    }

    private fun setupAction(){
        binding.launchCamera.setOnClickListener {
            startTakePhoto()
        }

        binding.launchGallery.setOnClickListener {
            startGallery()
        }

        binding.uploadButton.setOnClickListener {
            isLoading.observe(this) {
                showLoading(it)
            }
            val desc = binding.descText.text.toString()
            when{
                desc.isEmpty() -> {
                    binding.descText.error = getString(R.string.empty_field)
                } else -> {
                    uploadImage()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Permission Denied.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@NewStoryActivity,
                "com.raf.storyappsubmission",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = BitmapFactory.decodeFile(getFile?.path)
            rotateBitmap(
                BitmapFactory.decodeFile(myFile.path), true)
            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, this@NewStoryActivity)

            getFile = myFile

            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    private fun uploadImage() {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val token = intent.getStringExtra(EXTRA_TOKEN)
            val lat = intent.getStringExtra(EXTRA_LAT)?.toFloat()
            val lon = intent.getStringExtra(EXTRA_LON)?.toFloat()
            val description = binding.descText.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            val service = getApiService().uploadImage("bearer $token", imageMultipart, description, lat, lon)

            _isLoading.value = true
            service.enqueue(object : Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val intent = Intent(this@NewStoryActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        finish()

                        if (responseBody != null && !responseBody.error) {
                            Toast.makeText(this@NewStoryActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                        }
                        Timer("SettingUp", false).schedule(500) {
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@NewStoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    _isLoading.value = false
                    Toast.makeText(this@NewStoryActivity, "Failed instance Retrofit", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@NewStoryActivity, "Please, insert an image!.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

    }
}