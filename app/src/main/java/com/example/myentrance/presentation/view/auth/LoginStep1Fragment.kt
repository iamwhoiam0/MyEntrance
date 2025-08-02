package com.example.myentrance.presentation.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentLoginStep1Binding
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.presentation.viewmodel.AuthViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginStep1Fragment : Fragment() {

    private var _binding: FragmentLoginStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_graph) {
        AuthViewModelFactory(ProvideAuthRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendOtpButton.setOnClickListener {
            //val phone = binding.phoneInput.text.toString().trim()
            val phone = "+79999999999"
            if (phone.isBlank()) {
                Toast.makeText(context, "Введите номер телефона", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.checkUserExists(
                phone, onExists = {
                    onPhoneNumberExist()
                                  },
                onNotExists = {
                    Toast.makeText(context, "Номер не зарегистрирован. Просьба пройти регистрацию", Toast.LENGTH_SHORT).show()
                              },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_LoginStep1Fragment_to_registerStep1Fragment1)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onPhoneNumberExist(){
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(viewModel.phone.value)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    viewModel.markOtpSent(verificationId)
                    findNavController().navigate(R.id.action_LoginStep1Fragment_to_LoginStep2Fragment)
                }

                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Toast.makeText(context, "DA", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(context, "Ошибка отправки OTP", Toast.LENGTH_SHORT).show()

                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}