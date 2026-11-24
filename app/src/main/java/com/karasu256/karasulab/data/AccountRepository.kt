package com.karasu256.karasulab.data

import android.util.Log
import com.karasu256.karasulab.data.auth.SessionTokenExchanger
import com.karasu256.karasulab.data.local.AccountEntity
import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.SessionCookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AccountRepository @Inject constructor(
    private val authApi: AuthApi,
    private val store: SessionStore,
    private val exchanger: SessionTokenExchanger,
) {
    val accounts: Flow<List<Account>> = store.accounts.map { list -> list.map { it.toAccount() } }

    val activeAccount: Flow<Account?> = store.activeAccount.map { it?.toAccount() }

    suspend fun activeAccountId(): String? = store.currentAccount()?.id

    suspend fun ensureActiveAccount() {
        val token = store.currentToken() ?: return
        if (store.currentAccount() != null) return
        val cookie = SessionCookie(token.sessionCookieName, token.sessionCookieValue, token.sessionExpiresAtMs)
        if (exchanger.exchange(cookie) == null) store.clearActive()
    }

    suspend fun switchTo(id: String): Boolean {
        val account = store.account(id) ?: return false
        val cookie = SessionCookie(account.sessionCookieName, account.sessionCookieValue, account.sessionExpiresAtMs)
        if (exchanger.exchange(cookie) != null) return true
        store.removeAccount(id)
        return false
    }

    suspend fun remove(id: String) {
        val account = store.account(id) ?: return
        revoke(account)
        if (!account.isActive) {
            store.removeAccount(id)
            return
        }
        store.clearActive()
        for (next in store.allAccounts()) {
            if (switchTo(next.id)) return
        }
    }

    suspend fun signOutActive() {
        val id = activeAccountId() ?: return
        remove(id)
    }

    private suspend fun revoke(account: AccountEntity) {
        try {
            val cookie = SessionCookie(account.sessionCookieName, account.sessionCookieValue, account.sessionExpiresAtMs)
            authApi.signOut(cookie.toHeader()).also {
                it.body()?.close()
                it.errorBody()?.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AccountRepository", "Could not revoke the session of ${account.id}", e)
        }
    }

    private fun AccountEntity.toAccount() = Account(
        id = id,
        name = name,
        email = email,
        emailVerified = emailVerified,
        image = image,
        isActive = isActive,
    )
}
