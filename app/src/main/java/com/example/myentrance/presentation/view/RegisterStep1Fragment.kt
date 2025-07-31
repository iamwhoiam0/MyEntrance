package com.example.myentrance.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentRegisterStep1Binding
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.presentation.viewmodel.AuthViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class RegisterStep1Fragment : Fragment() {

    private var _binding: FragmentRegisterStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_graph) {
        AuthViewModelFactory(ProvideAuthRepository())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка "Продолжить"
        binding.continueButton.setOnClickListener {
            val phone = binding.phoneInput.text.toString().trim()
            val name = binding.nameInput.text.toString().trim()
            val apartment = binding.apartmentInput.text.toString().trim()
            val building = binding.buildingInput.text.toString().trim()
            viewModel.checkUserNotExists(
                phone = "+79850356722",
                onNotExists = {
                    viewModel.saveRegistrationDraft("+79850356722", "gamzat", "23", "37")
                    sendRegisterOtp("+79850356722")
                },
                onExists = {
                    Toast.makeText(context, "Регистрация для этого номера уже есть. Пожалуйста, войдите.", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
            // Простейшая валидация
            //if (phone.isBlank() || name.isBlank() || apartment.isBlank() || building.isBlank()) {
            //    Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            //    return@setOnClickListener
            //}
            //viewModel.saveRegistrationDraft(phone, name, apartment, building)
        }
    }

    // Метод отправки OTP для регистрации через Firebase
    private fun sendRegisterOtp(phone: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    viewModel.markOtpSent(verificationId)
                    findNavController().navigate(R.id.action_RegisterStep1Fragment_to_RegisterStep2Fragment)
                }
                override fun onVerificationCompleted(p0: PhoneAuthCredential) { }
                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(context, "Ошибка отправки OTP: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}