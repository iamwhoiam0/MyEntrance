package com.example.myentrance.presentation.view.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myentrance.R
import com.example.myentrance.databinding.FragmentMainBinding


class MainFragment : Fragment() {


    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    // Данные фрагменты (создавайте или получайте через DI)
    private val newsFragment = NewsFragment()
    private val chatFragment = ChatFragment()
    private val discussionsFragment = DiscussionsFragment()
    private val votesFragment = VotesFragment()

    private var activeFragment: Fragment = newsFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, votesFragment, "4")
            .hide(votesFragment)
            .commit()
        childFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, discussionsFragment, "3")
            .hide(discussionsFragment)
            .commit()
        childFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, chatFragment, "2")
            .commit()
        childFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, newsFragment, "1")
            .commit()

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_news ->{
                    switchFragment(newsFragment)
                    true
                }
                R.id.navigation_chat -> {
                    switchFragment(chatFragment)
                    true
                }

                R.id.navigation_discussions -> {
                    switchFragment(discussionsFragment)
                    true
                }

                R.id.navigation_votes -> {
                    switchFragment(votesFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(target: Fragment) {
        if (activeFragment != target) {
            childFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}