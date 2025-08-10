package com.example.myentrance.presentation.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myentrance.databinding.FragmentChatBinding
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.presentation.ChatAdapter
import com.example.myentrance.presentation.ChatHiltViewModelFactory
import com.example.myentrance.presentation.viewmodel.ChatViewModel
import com.example.myentrance.presentation.viewmodel.ChatViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    @Inject
    lateinit var chatHiltViewModelFactory: ChatHiltViewModelFactory

    private lateinit var chatViewModel: ChatViewModel

    @Inject
    lateinit var authRepository: AuthRepository


    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentUser = authRepository.getCurrentUser()
        val buildingId = currentUser?.buildingId ?: "default_building"

        chatViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return chatHiltViewModelFactory.createWithArgs(buildingId) as T
                }
            }
        )[ChatViewModel::class.java]

        val currentUserId = currentUser?.id ?: ""
        val adapter = ChatAdapter(currentUserId)
        binding.recyclerViewMessages.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { messages ->
                    adapter.submitList(messages)
                    if (messages.isNotEmpty()) binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            binding.editTextMessage.text?.let {
                chatViewModel.sendMessage(it.toString())
                it.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}