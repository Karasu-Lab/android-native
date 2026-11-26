package com.karasu256.karasulab.data.local

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SessionStore {
    val token: Flow<AuthTokenEntity?>

    val accounts: Flow<List<AccountEntity>>

    val activeAccount: Flow<AccountEntity?>

    suspend fun currentToken(): AuthTokenEntity?

    suspend fun currentAccount(): AccountEntity?

    suspend fun account(id: String): AccountEntity?

    suspend fun allAccounts(): List<AccountEntity>

    suspend fun saveSession(token: AuthTokenEntity, account: AccountEntity)

    suspend fun updateProfile(id: String, name: String?, image: String?)

    suspend fun updateEmailVerified(id: String, emailVerified: Boolean)

    suspend fun removeAccount(id: String)

    suspend fun clearActive()

    suspend fun clear()
}

class NotSignedInException : IllegalStateException("Not signed in")

suspend fun SessionStore.requireToken(): AuthTokenEntity =
    currentToken() ?: throw NotSignedInException()

suspend fun SessionStore.requireAccount(): AccountEntity =
    currentAccount() ?: throw NotSignedInException()

class RoomSessionStore @Inject constructor(
    private val database: AppDatabase,
) : SessionStore {
    override val token: Flow<AuthTokenEntity?> = database.authTokenDao().observe()

    override val accounts: Flow<List<AccountEntity>> = database.accountDao().observeAll()

    override val activeAccount: Flow<AccountEntity?> = database.accountDao().observeActive()

    override suspend fun currentToken(): AuthTokenEntity? = database.authTokenDao().get()

    override suspend fun currentAccount(): AccountEntity? = database.accountDao().getActive()

    override suspend fun account(id: String): AccountEntity? = database.accountDao().get(id)

    override suspend fun allAccounts(): List<AccountEntity> = database.accountDao().getAll()

    override suspend fun saveSession(token: AuthTokenEntity, account: AccountEntity) {
        database.withTransaction {
            val accounts = database.accountDao()
            val existing = accounts.get(account.id)
            accounts.deactivateAll()
            accounts.upsert(
                account.copy(
                    isActive = true,
                    addedAtMs = existing?.addedAtMs ?: account.addedAtMs
                )
            )
            database.authTokenDao().upsert(token)
        }
    }

    override suspend fun updateProfile(id: String, name: String?, image: String?) {
        database.accountDao().updateProfile(id, name, image)
    }

    override suspend fun updateEmailVerified(id: String, emailVerified: Boolean) {
        database.accountDao().updateEmailVerified(id, emailVerified)
    }

    override suspend fun removeAccount(id: String) {
        database.accountDao().delete(id)
    }

    override suspend fun clearActive() {
        database.withTransaction {
            database.accountDao().getActive()?.let { database.accountDao().delete(it.id) }
            database.authTokenDao().clear()
        }
    }

    override suspend fun clear() {
        database.withTransaction {
            database.authTokenDao().clear()
            database.accountDao().clear()
        }
    }
}
