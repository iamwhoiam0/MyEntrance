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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentMainBinding
import com.example.myentrance.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val tabOrder = listOf(
        R.id.newsFragment,
        R.id.chatFragment,
        R.id.profileFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.bottom_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            viewModel.onTabSelected(item.itemId)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateTo.collect { destinationId ->
                    val currentId = navController.currentDestination?.id
                    if (currentId == destinationId) return@collect

                    val currentIndex = currentId?.let { tabOrder.indexOf(it) } ?: -1
                    val destinationIndex = tabOrder.indexOf(destinationId)

                    val (enterAnim, exitAnim, popEnterAnim, popExitAnim) = if (destinationIndex > currentIndex) {
                        listOf(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                    } else {
                        listOf(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                    }

                    navController.navigate(
                        destinationId, null,
                        NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setRestoreState(true)
                            .setEnterAnim(enterAnim)
                            .setExitAnim(exitAnim)
                            .setPopEnterAnim(popEnterAnim)
                            .setPopExitAnim(popExitAnim)
                            .setPopUpTo(R.id.bottom_nav_graph, inclusive = true, saveState = true)
                            .build()
                    )

                    binding.bottomNavigation.selectedItemId = destinationId
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}