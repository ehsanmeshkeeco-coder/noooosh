package com.example.data.remote.supabase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SupabaseClientProviderTest {

    @Test
    fun testInitializationWithCredentials() {
        val provider = SupabaseClientProvider(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test_anon_key_12345"
        )

        assertTrue(provider.isConfigured)
        assertNotNull(provider.client)
        assertEquals("https://example.supabase.co", provider.client.supabaseUrl)
        assertEquals("test_anon_key_12345", provider.client.supabaseKey)
    }

    @Test
    fun testUnconfiguredStateWhenUrlOrKeyIsBlank() {
        val blankUrlProvider = SupabaseClientProvider(
            supabaseUrl = "",
            supabaseKey = "test_key"
        )
        assertFalse(blankUrlProvider.isConfigured)

        val placeholderProvider = SupabaseClientProvider(
            supabaseUrl = "https://YOUR_PROJECT.supabase.co",
            supabaseKey = "test_key"
        )
        assertFalse(placeholderProvider.isConfigured)
    }

    @Test
    fun testConnectionReturnsFailureWhenNotConfigured() = runBlocking {
        val unconfigured = SupabaseClientProvider(
            supabaseUrl = "",
            supabaseKey = ""
        )

        val result = unconfigured.testConnection()
        assertTrue(result is SupabaseConnectionResult.Failure)
        val failure = result as SupabaseConnectionResult.Failure
        assertTrue(failure.message.isNotEmpty())
    }

    @Test
    fun testSingletonInstanceAccess() {
        val instance1 = SupabaseClientProvider.getInstance()
        val instance2 = SupabaseClientProvider.getInstance()
        assertSame(instance1, instance2)
        assertNotNull(instance1.client)
    }
}
