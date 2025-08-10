package com.example.myentrance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myentrance.presentation.viewmodel.ChatViewModel
import com.example.myentrance.presentation.viewmodel.ChatViewModelFactory
import javax.inject.Inject

class ChatHiltViewModelFactory @Inject constructor(
    private val assistedFactory: ChatViewModelFactory
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        throw IllegalStateException("Use createWithArgs")
    }

    fun createWithArgs(buildingId: String): ChatViewModel {
        return assistedFactory.create(buildingId)
    }
}