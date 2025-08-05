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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val supabaseClient by lazy {
        (requireActivity().application as MyEntranceApp).supabaseClient
    }
    private val newsViewModel: NewsViewModel by viewModels {
        NewsViewModelFactory(
            ProvideNewsRepository(
                context = requireContext(),
                supabaseClient = supabaseClient
            ),
            ProvideAuthRepository(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NewsAdapter()
        binding.recyclerViewNews.adapter = adapter

        newsViewModel.loadNews()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.newsFeed.collectLatest { newsList ->
                     adapter.submitList(newsList)
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
