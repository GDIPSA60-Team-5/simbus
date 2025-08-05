package com.example.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSecureStorageManager(@ApplicationContext context: Context): SecureStorageManager =
        SecureStorageManager(context)
}
