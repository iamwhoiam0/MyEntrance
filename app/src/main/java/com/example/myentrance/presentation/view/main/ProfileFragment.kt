package com.example.myentrance.presentation.view.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.R
import com.example.myentrance.data.repository.ProfileRepositoryImpl
import com.example.myentrance.databinding.FragmentProfileBinding
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ProfileRepository
import com.example.myentrance.presentation.view.MainActivity
import com.example.myentrance.presentation.viewmodel.ProfileViewModel
import com.example.myentrance.presentation.viewmodel.ProfileViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authRepository: AuthRepository by lazy {
        ProvideAuthRepository(requireContext())
    }

    private val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(
            firestore = FirebaseFirestore.getInstance(),
            supabaseClient = (requireActivity().application as MyEntranceApp).supabaseClient,
            context = requireContext()
        )
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(profileRepository, authRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.user.collect { user ->
                    if (user != null) {
                        binding.editTextName.setText(user.name)
                        binding.editTextPhoneNumber.setText(user.phoneNumber)
                        binding.editTextDescription.setText(user.description)
                        binding.editTextApartment.setText(user.apartmentNumber)
                        binding.editTextBuilding.setText(user.buildingId)

                        Glide.with(binding.imageViewAvatar.context)
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.ic_profile)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.imageViewAvatar)
                    }
                }
            }
        }

        binding.buttonChangeAvatar.setOnClickListener {
            pickImageFromGallery()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.uploadAvatarResult.collect { result ->
                    if (result is Result.Success) {
                        Toast.makeText(requireContext(), "Аватар обновлен", Toast.LENGTH_SHORT).show()

                        Glide.with(binding.imageViewAvatar.context)
                            .load(result.data)
                            .placeholder(R.drawable.ic_profile)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.imageViewAvatar)
                    } else if (result is Result.Failure) {
                        Toast.makeText(requireContext(), "Ошибка загрузки аватара: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            val currentUser = profileViewModel.user.value ?: return@setOnClickListener
            val updatedUser = currentUser.copy(
                name = binding.editTextName.text.toString(),
                phoneNumber = binding.editTextPhoneNumber.text.toString(),
                description = binding.editTextDescription.text.toString(),
                apartmentNumber = binding.editTextApartment.text.toString(),
                buildingId = binding.editTextBuilding.text.toString()
            )
            profileViewModel.updateUser(updatedUser)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.updateResult.collect { result ->
                    if (result is Result.Success) {
                        Toast.makeText(requireContext(), "Профиль обновлен", Toast.LENGTH_SHORT).show()
                    } else if (result is Result.Failure) {
                        Toast.makeText(requireContext(), "Ошибка обновления профиля: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.buttonLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.logout()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.logoutEvent.collect {
                    try {
                        val navController = findNavController()

                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .setLaunchSingleTop(true)
                            .build()

                        navController.navigate(R.id.splashFragment, null, navOptions)

                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Ошибка навигации: ${e.message}", e)

                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profileViewModel.uploadAvatar(it)
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}