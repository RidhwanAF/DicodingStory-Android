package com.raf.storyappsubmission.detail

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.raf.storyappsubmission.databinding.ActivityDetailBinding
import com.raf.storyappsubmission.main.MainActivity

class DetailActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showTabLayout()
        setDetailStories()
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

    private fun setDetailStories(){
        val img = intent.getStringExtra(EXTRA_IMAGE)
        val id = intent.getStringExtra(EXTRA_ID)
        val created = intent.getStringExtra(EXTRA_CREATED)
        val name = intent.getStringExtra(EXTRA_NAME)
        val desc = intent.getStringExtra(EXTRA_DESC)
        val lat = intent.getFloatExtra(EXTRA_LAT, 0F)
        val lon = intent.getFloatExtra(EXTRA_LON, 0F)

        this.title = name

        Glide.with(this)
            .load(img)
            .into(binding.imageStory)
        binding.idStory.text = id
        binding.createdStory.text = created
        binding.nameStory.text = name
        binding.descStory.text = desc
        binding.tvLat.text = lat.toString()
        binding.tvLon.text = lon.toString()
        binding.descStory.movementMethod = ScrollingMovementMethod()
    }

    companion object {
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_CREATED = "extra_created"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
    }
}