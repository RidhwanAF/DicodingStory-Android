package com.raf.storyappsubmission.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raf.storyappsubmission.api.ListStoryItemResponse
import com.raf.storyappsubmission.databinding.ItemStoryBinding
import com.raf.storyappsubmission.detail.DetailActivity

class StoriesAdapter :
    PagingDataAdapter<ListStoryItemResponse, StoriesAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItemResponse) {
            Glide.with(itemView.context)
                .load(data.photoUrl)
                .into(binding.imageStory)
            binding.nameStory.text = data.name

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_IMAGE, data.photoUrl)
                intent.putExtra(DetailActivity.EXTRA_CREATED, data.createdAt)
                intent.putExtra(DetailActivity.EXTRA_NAME, data.name)
                intent.putExtra(DetailActivity.EXTRA_DESC, data.description)
                intent.putExtra(DetailActivity.EXTRA_ID, data.id)
                intent.putExtra(DetailActivity.EXTRA_LAT, data.lat)
                intent.putExtra(DetailActivity.EXTRA_LON, data.lon)
                itemView.context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity).toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItemResponse>() {
            override fun areItemsTheSame(oldItem: ListStoryItemResponse, newItem: ListStoryItemResponse): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItemResponse, newItem: ListStoryItemResponse): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}