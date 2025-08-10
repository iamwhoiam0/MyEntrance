package com.example.myentrance.presentation.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentRegisterStep1Binding
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.presentation.viewmodel.AuthViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.example.myentrance.utils.PhoneNumberFormattingTextWatcher
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import kotlin.getValue

@AndroidEntryPoint
class RegisterStep1Fragment : Fragment() {

    private var _binding: FragmentRegisterStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.phoneInput.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.continueButton.setOnClickListener {
//            val rawDigits = binding.phoneInput.text
//                ?.toString()
//                ?.replace(Regex("\\D"), "")  // оставляем только цифры
//                ?: ""
//
//            val phone = "+7$rawDigits"

            val phone = "+79850356722"//binding.phoneInput.text.toString().trim()
            val name = "abdul"//binding.nameInput.text.toString().trim()
            val apartment = "32"//binding.apartmentInput.text.toString().trim()
            val building = "15"//binding.buildingInput.text.toString().trim()

            if (phone.isBlank() || name.isBlank() || apartment.isBlank() || building.isBlank()) {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkUserNotExists(
                phone = phone,
                onNotExists = {
                    viewModel.saveRegistrationDraft(phone, name, apartment, building)
                    sendRegisterOtp(phone)
                },
                onExists = {
                    Toast.makeText(context, "Регистрация для этого номера уже есть. Пожалуйста, войдите.", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

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