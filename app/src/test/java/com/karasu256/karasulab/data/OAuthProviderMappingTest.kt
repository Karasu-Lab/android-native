package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.remote.OAuthProviderColors
import com.karasu256.karasulab.data.remote.OAuthProviderResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class OAuthProviderMappingTest {
    @Test
    fun mapsProviderDtosToDomainModelsGenerically() {
        val response = OAuthProviderResponse(
            id = "github",
            label = "GitHub",
            kind = "social",
            signIn = true,
            link = true,
            backgroundColor = OAuthProviderColors(default = "#fff", dark = "#000"),
            foregroundColor = OAuthProviderColors(default = "#000", dark = "#fff"),
            iconUrl = "https://api.example.com/github.svg",
        )

        assertEquals(
            OAuthProvider(
                id = "github",
                label = "GitHub",
                kind = "social",
                signIn = true,
                link = true,
                backgroundColor = ProviderColors(default = "#fff", dark = "#000"),
                foregroundColor = ProviderColors(default = "#000", dark = "#fff"),
                iconUrl = "https://api.example.com/github.svg",
            ),
            response.toModel(),
        )
    }
}
