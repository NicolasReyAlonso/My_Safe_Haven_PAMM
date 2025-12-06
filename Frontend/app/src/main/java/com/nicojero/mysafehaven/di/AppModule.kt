package com.nicojero.mysafehaven.di

import android.content.Context
import com.nicojero.mysafehaven.data.local.AuthDataStore
import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.RetrofitClient
import com.nicojero.mysafehaven.data.repository.AuthRepository
import com.nicojero.mysafehaven.data.repository.HavenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): AuthDataStore {
        return AuthDataStore(context)
    }

    @Provides
    @Singleton
    fun provideApiService(authDataStore: AuthDataStore): ApiService {
        return RetrofitClient.createApiService(authDataStore)
    }
}
