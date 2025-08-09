package com.example.myentrance

import android.app.Application
import com.example.myentrance.presentation.UserSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class MyEntranceApp : Application() {

    val supabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Storage)
        }
    }

    val userSessionManager by lazy {
        UserSessionManager(this)
    }
}