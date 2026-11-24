package com.karasu256.karasulab.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM account ORDER BY addedAtMs")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<AccountEntity?>

    @Query("SELECT * FROM account WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): AccountEntity?

    @Query("SELECT * FROM account WHERE id = :id")
    suspend fun get(id: String): AccountEntity?

    @Query("SELECT * FROM account ORDER BY addedAtMs")
    suspend fun getAll(): List<AccountEntity>

    @Upsert
    suspend fun upsert(account: AccountEntity)

    @Query("UPDATE account SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE account SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: String)

    @Query("UPDATE account SET name = :name, image = :image WHERE id = :id")
    suspend fun updateProfile(id: String, name: String?, image: String?)

    @Query("UPDATE account SET emailVerified = :emailVerified WHERE id = :id")
    suspend fun updateEmailVerified(id: String, emailVerified: Boolean)

    @Query("DELETE FROM account WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM account")
    suspend fun clear()
}
