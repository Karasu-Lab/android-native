package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.ChangePasswordRequest
import com.karasu256.karasulab.data.remote.DeleteUserRequest
import com.karasu256.karasulab.data.remote.SetPasswordRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionCookie: ActiveSessionCookie,
    private val accountRepository: AccountRepository,
) {
    suspend fun settings(): SecuritySettings {
        val response = apiCall { authApi.securitySettings(sessionCookie.header()) }
        return SecuritySettings(
            hasPassword = response.hasPassword,
            passkeys = response.passkeys.map {
                Passkey(
                    id = it.id,
                    name = it.name,
                    createdOn = it.createdAt?.take(10)
                )
            },
        )
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        authApi.changePassword(
            sessionCookie.header(),
            ChangePasswordRequest(currentPassword, newPassword)
        ).ensureSuccess()
    }

    suspend fun setPassword(newPassword: String) {
        authApi.setPassword(sessionCookie.header(), SetPasswordRequest(newPassword)).ensureSuccess()
    }

    suspend fun deleteAccount(password: String?) {
        authApi.deleteUser(sessionCookie.header(), DeleteUserRequest(password)).ensureSuccess()
        accountRepository.signOutActive()
    }
}
