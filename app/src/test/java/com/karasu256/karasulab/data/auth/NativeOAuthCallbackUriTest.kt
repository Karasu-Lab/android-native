package com.karasu256.karasulab.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NativeOAuthCallbackUriTest {
    @Test
    fun createsProviderCallbackAndAuthUiUrls() {
        val state = "native-state-1234567890"

        assertEquals(
            "karasulab://api/auth/callback/github?state=$state",
            NativeOAuthCallbackUri.callback("github", state),
        )
        assertEquals(
            "https://auth.karasu256.com/sign-in?provider=github&prompt=login&next=karasulab%3A%2F%2Fapi%2Fauth%2Fcallback%2Fgithub%3Fstate%3D$state",
            NativeOAuthCallbackUri.signInUrl("github", state, "auth.karasu256.com"),
        )
    }

    @Test
    fun parsesOneTimeTokenAndRejectsMismatchedOrDuplicateState() {
        val state = "native-state-1234567890"
        val callback = NativeOAuthCallbackUri.parse(
            "karasulab://api/auth/callback/github?state=$state&token=token.signature1",
        )

        assertNotNull(callback)
        assertEquals("github", callback?.providerId)
        assertEquals(state, callback?.state)
        assertEquals("token.signature1", callback?.token)
        val linkedCallback = NativeOAuthCallbackUri.parse(
            "karasulab://api/auth/callback/github?state=$state&linked=github",
        )
        assertEquals("github", linkedCallback?.linked)
        assertNull(NativeOAuthCallbackUri.parse("karasulab://attacker/auth/callback/github?state=$state"))
        assertNull(NativeOAuthCallbackUri.parse("karasulab://api/auth/callback/github?state=$state&state=$state"))
        assertFalse(NativeOAuthCallbackUri.newState().contains("="))
    }
}
