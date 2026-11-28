package com.karasu256.karasulab.di

import com.karasu256.karasulab.data.FirebaseRepository
import com.karasu256.karasulab.data.NoGmsFirebaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    @Binds
    @Singleton
    abstract fun bindFirebaseRepository(repository: NoGmsFirebaseRepository): FirebaseRepository
}
