package com.example.myentrance.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myentrance.R
import com.example.myentrance.databinding.ItemChatDateSeparatorBinding
import com.example.myentrance.databinding.ItemChatImageBinding
import com.example.myentrance.databinding.ItemChatTextBinding
import com.example.myentrance.domain.entities.ChatItem
import com.example.myentrance.domain.entities.ChatMessageWithProfile

class ChatAdapter(private val currentUserId: String) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_TEXT = 1
        private const val TYPE_IMAGE = 2
        private const val TYPE_DATE_SEPARATOR = 3

        class DiffCallback : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return when {
                    oldItem is ChatItem.Message && newItem is ChatItem.Message ->
                        oldItem.message.id == newItem.message.id
                    oldItem is ChatItem.DateSeparator && newItem is ChatItem.DateSeparator ->
                        oldItem.date == newItem.date
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem) = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.DateSeparator -> TYPE_DATE_SEPARATOR
            is ChatItem.Message -> {
                if (item.message.imageUrl != null) TYPE_IMAGE else TYPE_TEXT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_SEPARATOR -> {
                val binding = ItemChatDateSeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateSeparatorViewHolder(binding)
            }
            TYPE_TEXT -> {
                val binding = ItemChatTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextMessageViewHolder(binding)
            }
            TYPE_IMAGE -> {
                val binding = ItemChatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatItem.DateSeparator -> {
                (holder as DateSeparatorViewHolder).bind(item.date)
            }
            is ChatItem.Message -> {
                val isMine = item.message.senderId == currentUserId
                when (holder) {
                    is TextMessageViewHolder -> holder.bind(item.message, isMine)
                    is ImageMessageViewHolder -> holder.bind(item.message, isMine)
                }
            }
        }
    }

    class DateSeparatorViewHolder(private val binding: ItemChatDateSeparatorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.textViewDate.text = date
        }
    }

    class TextMessageViewHolder(private val binding: ItemChatTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageWithProfile, isMine: Boolean) {
            binding.textViewMessage.text = message.text
            binding.textViewTime.text = message.getFormattedTime()

            val cardLayoutParams = binding.messageCard.layoutParams as ConstraintLayout.LayoutParams

            if (isMine) {
                // Мое сообщение - справа
                cardLayoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                cardLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                cardLayoutParams.horizontalBias = 1.0f
                cardLayoutParams.marginStart = 80
                cardLayoutParams.marginEnd = 8

                // Цвета для моего сообщения
                binding.messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                )
                binding.textViewMessage.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOnPrimary)
                )
                binding.textViewTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOnPrimary)
                )

                // Скрываем аватар и имя
                binding.avatarContainer.visibility = android.view.View.GONE
                binding.textViewSender.visibility = android.view.View.GONE

            } else {
                // Чужое сообщение - слева
                cardLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                cardLayoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                cardLayoutParams.horizontalBias = 0.0f
                cardLayoutParams.marginStart = 8
                cardLayoutParams.marginEnd = 80

                // Стандартные цвета
                binding.messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorSurface)
                )
                binding.textViewMessage.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOnSurface)
                )
                binding.textViewTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOutline)
                )

                // Показываем аватар и имя
                binding.avatarContainer.visibility = android.view.View.VISIBLE
                binding.textViewSender.visibility = android.view.View.VISIBLE
                binding.textViewSender.text = message.senderName

                // Загружаем аватар
                Glide.with(binding.imageViewSenderAvatar.context)
                    .load(message.senderAvatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imageViewSenderAvatar)
            }

            binding.messageCard.layoutParams = cardLayoutParams
        }
    }

    class ImageMessageViewHolder(private val binding: ItemChatImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageWithProfile, isMine: Boolean) {
            Glide.with(binding.imageViewMessage.context)
                .load(message.imageUrl)
                .into(binding.imageViewMessage)

            binding.textViewTime.text = message.getFormattedTime()

            val cardLayoutParams = binding.messageCard.layoutParams as ConstraintLayout.LayoutParams

            if (isMine) {
                // Мое сообщение - справа
                cardLayoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                cardLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                cardLayoutParams.horizontalBias = 1.0f
                cardLayoutParams.marginStart = 80
                cardLayoutParams.marginEnd = 8

                binding.messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                )
                binding.textViewTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOnPrimary)
                )

                // Скрываем аватар и имя
                binding.avatarContainer.visibility = android.view.View.GONE
                binding.textViewSender.visibility = android.view.View.GONE

            } else {
                // Чужое сообщение - слева
                cardLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                cardLayoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                cardLayoutParams.horizontalBias = 0.0f
                cardLayoutParams.marginStart = 8
                cardLayoutParams.marginEnd = 80

                binding.messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorSurface)
                )
                binding.textViewTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorOutline)
                )

                // Показываем аватар и имя
                binding.avatarContainer.visibility = android.view.View.VISIBLE
                binding.textViewSender.visibility = android.view.View.VISIBLE
                binding.textViewSender.text = message.senderName

                Glide.with(binding.imageViewSenderAvatar.context)
                    .load(message.senderAvatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imageViewSenderAvatar)
            }

            binding.messageCard.layoutParams = cardLayoutParams
        }
    }
}