package com.example.myentrance.presentation.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.R
import com.example.myentrance.presentation.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userSessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        userSessionManager = (application as MyEntranceApp).userSessionManager

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Проверяем состояние аутентификации при старте
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        lifecycleScope.launch {
            userSessionManager.loadSession()

            try {
                val navController = findNavController(R.id.nav_host_fragment)
                val currentUser = userSessionManager.currentUser.value
                val firebaseUser = FirebaseAuth.getInstance().currentUser

                if (currentUser == null || firebaseUser == null) {
                    //
                }

            } catch (e: Exception) {
            }
        }
    }
}
