package com.karasu256.karasulab.di

import android.content.Context
import androidx.room.Room
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.auth.Base64JwtExpiry
import com.karasu256.karasulab.data.auth.JwtExpiry
import com.karasu256.karasulab.data.local.AppDatabase
import com.karasu256.karasulab.data.local.RoomSessionStore
import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.CustomTokenAuthenticator
import com.karasu256.karasulab.data.remote.CustomTokenInterceptor
import com.karasu256.karasulab.data.remote.HttpErrorLoggingInterceptor
import com.karasu256.karasulab.data.remote.OriginInterceptor
import com.karasu256.karasulab.data.remote.UserApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val jsonMediaType = "application/json".toMediaType()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    @AuthHttpClient
    fun provideAuthHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(OriginInterceptor("https://${BuildConfig.AUTH_HOST}"))
        .addNetworkInterceptor(HttpErrorLoggingInterceptor())
        .build()

    @Provides
    @Singleton
    @ApiHttpClient
    fun provideApiHttpClient(
        interceptor: CustomTokenInterceptor,
        authenticator: CustomTokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addNetworkInterceptor(HttpErrorLoggingInterceptor())
        .authenticator(authenticator)
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthHttpClient client: OkHttpClient,
        json: Json,
    ): AuthApi = Retrofit.Builder()
        .baseUrl("https://${BuildConfig.AUTH_HOST}/")
        .client(client)
        .addConverterFactory(json.asConverterFactory(jsonMediaType))
        .build()
        .create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(
        @ApiHttpClient client: OkHttpClient,
        json: Json,
    ): UserApi = Retrofit.Builder()
        .baseUrl("https://${BuildConfig.API_HOST}/")
        .client(client)
        .addConverterFactory(json.asConverterFactory(jsonMediaType))
        .build()
        .create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds
    @Singleton
    abstract fun bindSessionStore(store: RoomSessionStore): SessionStore

    @Binds
    abstract fun bindJwtExpiry(expiry: Base64JwtExpiry): JwtExpiry
}
