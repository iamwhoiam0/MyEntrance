package com.example.myentrance.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myentrance.R
import com.example.myentrance.domain.entities.News

class NewsAdapter : ListAdapter<News, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = getItem(position)
        holder.bind(newsItem)
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.textNewsTitle)
        private val imageView: ImageView = itemView.findViewById(R.id.imageNewsPhoto)

        fun bind(news: News) {
            titleText.text = news.text
            if (news.imageUrl != null) {
                imageView.visibility = View.VISIBLE
                imageView.load(news.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_placeholder)
                    error(R.drawable.ic_error)
                }
            } else {
                imageView.visibility = View.GONE
            }
        }
    }
}

class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }
}