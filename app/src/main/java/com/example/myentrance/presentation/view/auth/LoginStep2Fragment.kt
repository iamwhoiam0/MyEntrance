package com.example.myentrance.presentation.view.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentLoginStep2Binding
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class LoginStep2Fragment : Fragment() {

    private var _binding: FragmentLoginStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by hiltNavGraphViewModels(R.id.auth_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmOtpButton.setOnClickListener {
            viewModel.loginWithOtp(binding.otpInput.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthResult.Loading -> {
                        // Добавить в дальнейшем прогресс бар
                        binding.confirmOtpButton.isClickable = false
                    }
                    is AuthResult.Success -> {
                        viewModel.resetState()
                        try {
                            findNavController().navigate(R.id.action_LoginStep2Fragment_to_mainFragment)
                        } catch (e: Exception) {
                            Log.e("LoginStep2Fragment","Ошибка навигации: ${e.message}", e)
                        }

                        Toast.makeText(context, "Вход выполнен!", Toast.LENGTH_SHORT).show()
                    }
                    is AuthResult.Error -> {
                        binding.confirmOtpButton.isClickable = true
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // не знаю как избавиться от блока, пока пусть будет так :)
                    }
                    }
                }
            }
        }
    }
}