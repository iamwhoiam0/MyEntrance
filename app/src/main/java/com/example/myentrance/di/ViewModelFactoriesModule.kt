package com.example.myentrance.di

import com.example.myentrance.presentation.ChatHiltViewModelFactory
import com.example.myentrance.presentation.viewmodel.ChatViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewModelFactoriesModule {

    @Provides
    @Singleton
    fun provideChatHiltViewModelFactory(
        assistedFactory: ChatViewModelFactory
    ): ChatHiltViewModelFactory = ChatHiltViewModelFactory(assistedFactory)
}
