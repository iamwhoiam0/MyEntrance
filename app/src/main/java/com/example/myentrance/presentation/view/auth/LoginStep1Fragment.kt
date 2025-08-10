package com.example.myentrance.presentation.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentLoginStep1Binding
import com.example.myentrance.presentation.viewmodel.AuthViewModel
import com.example.myentrance.utils.PhoneNumberFormattingTextWatcher
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginStep1Fragment : Fragment() {

    private var _binding: FragmentLoginStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by hiltNavGraphViewModels(R.id.auth_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneInput.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.sendOtpButton.setOnClickListener {
            val rawDigits = binding.phoneInput.text
                ?.toString()
                ?.replace(Regex("\\D"), "")  // оставляем только цифры
                ?: ""

            val phone = "+7$rawDigits"
            if (phone == "+7") {
                Toast.makeText(context, "Введите номер телефона", Toast.LENGTH_SHORT).show()
                //binding.phoneInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.colorError)
                return@setOnClickListener
            }
            viewModel.checkUserExists(
                phone, onExists = {
                    onPhoneNumberExist()
                                  },
                onNotExists = {
                    Toast.makeText(context, "Номер не зарегистрирован. Просьба пройти регистрацию", Toast.LENGTH_SHORT).show()
                    //binding.phoneInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.colorError)
                              },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    //binding.phoneInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.colorError)
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