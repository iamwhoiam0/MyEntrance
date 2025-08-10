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
import com.example.myentrance.domain.entities.NewsWithUser
import java.util.Date
import java.util.Locale

    class NewsAdapter : ListAdapter<NewsWithUser, NewsAdapter.NewsViewHolder>(NewsWithUserDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            return NewsViewHolder(view)
        }

        override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
            val item = getItem(position)
            holder.bind(item)
        }

        class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.imageNewsPhoto)
            private val titleText: TextView = itemView.findViewById(R.id.textNewsTitle)
            private val authorAvatar: ImageView = itemView.findViewById(R.id.imageAuthorAvatar)
            private val authorName: TextView = itemView.findViewById(R.id.textAuthorName)
            private val newsDate: TextView = itemView.findViewById(R.id.textNewsDate)

            fun bind(newsWithUser: NewsWithUser) {
                val news = newsWithUser.news

                // Текст новости
                titleText.text = news.text

                // Изображение новости
                if (!news.imageUrl.isNullOrEmpty()) {
                    imageView.visibility = View.VISIBLE
                    imageView.load(news.imageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_placeholder)
                        error(R.drawable.ic_error)
                    }
                } else {
                    imageView.visibility = View.GONE
                }

                // Имя автора
                authorName.text = newsWithUser.userName.ifBlank { "Неизвестный автор" }

                // Аватар автора
                if (!newsWithUser.userAvatarUrl.isNullOrEmpty()) {
                    authorAvatar.load(newsWithUser.userAvatarUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                    }
                } else {
                    authorAvatar.setImageResource(R.drawable.ic_profile)
                }

                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale("ru"))
                newsDate.text = sdf.format(Date(news.createdAt))
            }
        }
    }

    class NewsWithUserDiffCallback : DiffUtil.ItemCallback<NewsWithUser>() {
        override fun areItemsTheSame(oldItem: NewsWithUser, newItem: NewsWithUser): Boolean {
            return oldItem.news.id == newItem.news.id
        }
        override fun areContentsTheSame(oldItem: NewsWithUser, newItem: NewsWithUser): Boolean {
            return oldItem == newItem
        }
    }
