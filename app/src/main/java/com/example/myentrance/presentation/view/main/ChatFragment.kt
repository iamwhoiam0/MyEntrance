package com.example.myentrance.presentation.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.databinding.FragmentChatBinding
import com.example.myentrance.presentation.ChatAdapter
import com.example.myentrance.presentation.viewmodel.ChatViewModel
import com.example.myentrance.presentation.viewmodel.ChatViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideChatRepository
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ChatRepository
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val supabaseClient by lazy {
        (requireActivity().application as MyEntranceApp).supabaseClient
    }

    private val authRepository: AuthRepository by lazy {
        ProvideAuthRepository(requireContext())
    }

    private lateinit var chatRepository: ChatRepository
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = authRepository.getCurrentUser()
        val buildingId = currentUser?.buildingId ?: "default_building"

        chatRepository = ProvideChatRepository(requireContext(), supabaseClient, buildingId)

        val firestore = FirebaseFirestore.getInstance()

        chatViewModel = ViewModelProvider(
            this,
            ChatViewModelFactory(chatRepository, authRepository, firestore)
        )[ChatViewModel::class.java]

        val currentUserId = currentUser?.id ?: ""
        adapter = ChatAdapter(currentUserId)
        binding.recyclerViewMessages.adapter = adapter

        // Подписка на обновление сообщений
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { messages ->
                    adapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            binding.editTextMessage.text?.let {
                chatViewModel.sendMessage(it.toString())
                it.clear()
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

        // TODO: Кнопка вложения, обработка загрузки фото
        binding.buttonAttach.setOnClickListener {

        }

        // Обработка результата загрузки вложения
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                chatViewModel.uploadAttachmentResult.collect { result ->
//                    if (result is Result.Success) {
//                        chatViewModel.sendMessage("", result.data) // Отправляем пустое сообщение с картинкой
//                    } else if (result is Result.Failure) {
//                        Toast.makeText(context, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}