package com.example.myentrance.presentation.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentRegisterStep2Binding
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.presentation.viewmodel.AuthViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterStep2Fragment : Fragment() {

    private var _binding: FragmentRegisterStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_graph) {
        AuthViewModelFactory(ProvideAuthRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmRegisterOtpButton.setOnClickListener {
            val otp = binding.otpInput.text.toString()
            if (otp.isEmpty()) {
                Toast.makeText(context, "Введите код", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.registerWithOtp(otp)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collectLatest { state ->
                    when (state) {
                        is AuthResult.Success -> {
                            try {
                                viewModel.resetState()
                                findNavController().navigate(R.id.action_registerStep2Fragment_to_main_nav_graph)
                                Toast.makeText(context, "Регистрация успешно завершена!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        is AuthResult.Error -> {
                            Toast.makeText(context, "Ошибка: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                        is AuthResult.Loading -> {
                            // Показывайте loader при необходимости
                        }
                        null -> {}
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
