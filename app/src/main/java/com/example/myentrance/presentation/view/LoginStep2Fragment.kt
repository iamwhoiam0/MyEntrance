package com.example.myentrance.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentLoginStep1Binding
import com.example.myentrance.databinding.FragmentLoginStep2Binding
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.presentation.viewmodel.AuthViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginStep2Fragment : Fragment() {

    private var _binding: FragmentLoginStep2Binding? = null
    private val binding get() = _binding!!

    // Используйте DI или создайте вручную
    private val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_graph) {
        AuthViewModelFactory(ProvideAuthRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmOtpButton.setOnClickListener {
            viewModel.loginWithOtp(binding.otpInput.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthResult.Loading -> {
                        // Показывай прогресс-бар, делай поля неактивными
                        binding.confirmOtpButton.isClickable = false
                    }
                    is AuthResult.Success -> {
                        viewModel.resetState()
                        // Навигация на главный экран или показ успеха:
                        findNavController().navigate(R.id.action_LoginStep2Fragment_to_mainFragment)
                        Toast.makeText(context, "Вход выполнен!", Toast.LENGTH_SHORT).show()
                    }
                    is AuthResult.Error -> {
                        // Показывай ошибку пользователю
                        binding.confirmOtpButton.isClickable = true
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // Нет состояния – ничего не делаем
                    }
                    }
                }
            }
        }
    }
}