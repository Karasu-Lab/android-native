package com.karasu256.karasulab.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthTokenDao {
    @Query("SELECT * FROM auth_token WHERE id = ${AuthTokenEntity.SINGLETON_ID}")
    fun observe(): Flow<AuthTokenEntity?>

    @Query("SELECT * FROM auth_token WHERE id = ${AuthTokenEntity.SINGLETON_ID}")
    suspend fun get(): AuthTokenEntity?

    @Upsert
    suspend fun upsert(token: AuthTokenEntity)

    @Query("DELETE FROM auth_token")
    suspend fun clear()
}
