package com.karasu256.karasulab.di

import com.karasu256.karasulab.init.AppInitializer
import com.karasu256.karasulab.init.FirebaseInitializer
import com.karasu256.karasulab.init.IInitializable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object InitializerModule {
    @Provides
    fun provideInitializers(
        app: AppInitializer,
        firebase: FirebaseInitializer,
    ): List<@JvmSuppressWildcards IInitializable> = listOf(app, firebase)
}
