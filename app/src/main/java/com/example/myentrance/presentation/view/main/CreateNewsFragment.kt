package com.example.myentrance.presentation.view.main

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myentrance.databinding.FragmentCreateNewsBinding
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.presentation.viewmodel.NewsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
@AndroidEntryPoint
class CreateNewsFragment : Fragment() {

    private var _binding: FragmentCreateNewsBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private val newsViewModel: NewsViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Покажите превью изображения, например:
            binding.imageViewPreview.setImageURI(imageUri)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                binding.imageViewPreview.setImageURI(it)
            }
        } else {
            imageUri = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSelectPhoto.setOnClickListener {
            val options = arrayOf("Выбрать из галереи", "Сделать фото")
            AlertDialog.Builder(requireContext())
                .setTitle("Добавить фото")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> {
                            imageUri = createImageUri()  // функция для создания Uri
                            imageUri?.let { takePhotoLauncher.launch(it) }
                        }
                    }
                }.show()
        }

        binding.buttonSendNews.setOnClickListener {
            val text = binding.editTextNewsText.text.toString().trim()
            if (text.isEmpty() && imageUri == null) {
                Toast.makeText(context, "Введите текст или добавьте фото", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            newsViewModel.addNews(text, imageUri)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.addNewsResult.collectLatest { result ->
                    if (result is Result.Failure) {
                        Log.e("NewsFragment", "Ошибка добавления новости", result.exception)
                        Toast.makeText(context, "Ошибка добавления новости: ${result.exception.localizedMessage ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Новость добавлена", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createImageUri(): Uri? {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "news_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
}
