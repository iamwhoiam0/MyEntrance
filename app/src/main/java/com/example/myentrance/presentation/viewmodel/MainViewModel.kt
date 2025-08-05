package com.example.myentrance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _navigateTo = MutableSharedFlow<Int>()
    val navigateTo = _navigateTo.asSharedFlow()

    fun onTabSelected(destinationId: Int) {
        viewModelScope.launch {
            _navigateTo.emit(destinationId)
        }
    }
}