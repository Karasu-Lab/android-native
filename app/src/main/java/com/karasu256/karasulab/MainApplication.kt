package com.karasu256.karasulab

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.karasu256.karasulab.data.FirebaseRepository
import com.karasu256.karasulab.logging.Log
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        super.onCreate()
        Log.install(firebaseRepository)
        firebaseRepository.setCrashlyticsCollectionEnabled(true)
    }

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
