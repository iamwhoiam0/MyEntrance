package com.example.myentrance.presentation.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentNewsBinding
import com.example.myentrance.presentation.NewsAdapter
import com.example.myentrance.presentation.viewmodel.NewsViewModel
import com.example.myentrance.presentation.viewmodel.NewsViewModelFactory
import com.example.myentrance.presentation.viewmodel.ProvideAuthRepository
import com.example.myentrance.presentation.viewmodel.ProvideNewsRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentNewsBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = NewsAdapter()
        binding.recyclerViewNews.adapter = adapter

        newsViewModel.loadNews()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.newsFeed.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

        binding.buttonAdd.setOnClickListener {
            findNavController().navigate(R.id.action_newsFragment_to_createNewsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

