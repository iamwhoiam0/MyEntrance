package com.example.myentrance.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myentrance.databinding.ItemChatImageBinding
import com.example.myentrance.databinding.ItemChatTextBinding
import com.example.myentrance.domain.entities.ChatMessage

class ChatAdapter(private val currentUserId: String) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_TEXT = 1
        private const val TYPE_IMAGE = 2

        class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.imageUrl != null) TYPE_IMAGE else TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            val binding = ItemChatTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TextMessageViewHolder(binding)
        } else {
            val binding = ItemChatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ImageMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is TextMessageViewHolder) holder.bind(message, message.senderId == currentUserId)
        if (holder is ImageMessageViewHolder) holder.bind(message, message.senderId == currentUserId)
    }

    class TextMessageViewHolder(private val binding: ItemChatTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, isMine: Boolean) {
            binding.textViewMessage.text = message.text
            //binding.root.gravity = if (isMine) Gravity.END else Gravity.START  // Поправить реализацию чтобы текст сдвигался в зависимости от отправителя
            binding.textViewSender.text = message.senderName
        }
    }

    class ImageMessageViewHolder(private val binding: ItemChatImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, isMine: Boolean) {
            Glide.with(binding.imageViewMessage.context)
                .load(message.imageUrl)
                .into(binding.imageViewMessage)

            //binding.root.gravity = if (isMine) Gravity.END else Gravity.START // Поправить реализацию чтобы текст сдвигался в зависимости от отправителя
            binding.textViewSender.text = message.senderName
        }
    }
}
