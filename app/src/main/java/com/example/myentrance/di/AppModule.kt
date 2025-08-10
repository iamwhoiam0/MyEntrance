package com.example.myentrance.di

import android.app.Application
import com.example.myentrance.MyEntranceApp
import com.example.myentrance.data.repository.AuthRepositoryImpl
import com.example.myentrance.data.repository.NewsRepositoryImpl
import com.example.myentrance.data.repository.ProfileRepositoryImpl
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.NewsRepository
import com.example.myentrance.domain.repository.ProfileRepository
import com.example.myentrance.presentation.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(app: Application): SupabaseClient = (app as MyEntranceApp).supabaseClient

    @Provides
    @Singleton
    fun provideUserSessionManager(app: Application): UserSessionManager = (app as MyEntranceApp).userSessionManager

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userSessionManager: UserSessionManager
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore, userSessionManager)

    @Provides
    @Singleton
    fun provideNewsRepository(
        supabaseClient: SupabaseClient,
        app: Application,
        firestore: FirebaseFirestore
    ): NewsRepository = NewsRepositoryImpl(supabaseClient, app, firestore)

    @Provides
    @Singleton
    fun provideProfileRepository(
        supabaseClient: SupabaseClient,
        app: Application,
        firestore: FirebaseFirestore
    ): ProfileRepository = ProfileRepositoryImpl(firestore, supabaseClient, app)
}
