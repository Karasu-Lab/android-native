package com.karasu256.karasulab.data.remote

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class OAuthLinkRequestSerializationTest {
    @Test
    fun serializesTheNativeOAuthLinkApiContract() {
        val callback = "karasulab://api/auth/callback/github?state=native-state-1234567890"

        assertEquals(
            """{"provider":"github","callbackURL":"$callback"}""",
            Json.encodeToString(OAuthLinkRequest(provider = "github", callbackURL = callback)),
        )
    }
}
