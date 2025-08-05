package com.example.myentrance.presentation.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.databinding.FragmentChatBinding
import com.example.myentrance.presentation.ChatAdapter
import com.example.myentrance.presentation.viewmodel.ChatViewModel
import com.example.myentrance.presentation.viewmodel.ChatViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideChatRepository
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val supabaseClient by lazy {
        (requireActivity().application as MyEntranceApp).supabaseClient
    }
    private val authRepository by lazy {
        ProvideAuthRepository(requireContext())
    }

    private val chatRepository by lazy {
        ProvideChatRepository(requireContext(), supabaseClient)
    }
    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(chatRepository, authRepository)
    }

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = authRepository.getCurrentUser()?.id ?: ""
        adapter = ChatAdapter(currentUserId)
        binding.recyclerViewMessages.adapter = adapter

        // подписка на обновление
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { messages ->
                    adapter.submitList(messages)
                    // Прокрутка к последнему сообщению
                    if (messages.isNotEmpty()) {
                        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(text)
                binding.editTextMessage.text.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.sendMessageResult.collect { result ->
                    if (result is Result.Failure) {
                        Toast.makeText(context, "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // TODO: Кнопка вложения, обработка загрузки фото:
        binding.buttonAttach.setOnClickListener {
            // Здесь должен быть вызов диалога выбора картинки и передача URI в chatViewModel.uploadAttachment
            // После получения результата вызов sendMessage с imageUrl
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.uploadAttachmentResult.collect { result ->
                    if (result is Result.Success) {
                        chatViewModel.sendMessage("", result.data)
                    } else if (result is Result.Failure) {
                        Toast.makeText(context, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

