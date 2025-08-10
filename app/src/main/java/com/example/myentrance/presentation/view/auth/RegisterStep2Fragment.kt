package com.example.myentrance.presentation.view.auth

import android.os.Bundle
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
import com.example.myentrance.databinding.FragmentRegisterStep2Binding
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class RegisterStep2Fragment : Fragment() {

    private var _binding: FragmentRegisterStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by hiltNavGraphViewModels(R.id.auth_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

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
                                findNavController().navigate(R.id.action_RegisterStep2Fragment_to_mainFragment)
                                Toast.makeText(context, "Регистрация успешно завершена!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        is AuthResult.Error -> {
                            Toast.makeText(context, "Ошибка: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                        is AuthResult.Loading -> {
                            // Добавить загрузчик
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
