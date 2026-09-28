package com.example.data.remote.supabase

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Result state returned by [SupabaseClientProvider.testConnection].
 */
sealed class SupabaseConnectionResult {
    data class Success(
        val message: String = "اتصال به سوپابیس با موفقیت برقرار شد.",
        val statusCode: Int = 200,
        val latencyMs: Long = 0L
    ) : SupabaseConnectionResult()

    data class Failure(
        val message: String,
        val statusCode: Int? = null,
        val error: Throwable? = null
    ) : SupabaseConnectionResult()
}

/**
 * Provider class that initializes and configures [SupabaseClient] using
 * credentials from [BuildConfig]. Also provides a [testConnection] method
 * to verify network reachability and authentication with the Supabase project.
 */
class SupabaseClientProvider(
    val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    val supabaseKey: String = BuildConfig.SUPABASE_SERVICE_ROLE_KEY.ifBlank { BuildConfig.SUPABASE_ANON_KEY }
) {
    /**
     * Lazily initialized [SupabaseClient] using the provided URL and Key.
     */
    val client: SupabaseClient by lazy {
        SupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        )
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                supabaseKey.isNotBlank() &&
                !supabaseUrl.contains("YOUR_") &&
                !supabaseUrl.contains("placeholder")

    /**
     * Performs a network ping/query to the Supabase REST API endpoint to verify
     * that the backend is reachable and the credentials are valid.
     *
     * @return [SupabaseConnectionResult.Success] if HTTP 200 is returned,
     *         or [SupabaseConnectionResult.Failure] with details on failure.
     */
    suspend fun testConnection(): SupabaseConnectionResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            val errorMsg = "آدرس یا کلید دسترسی به Supabase در BuildConfig تعریف نشده است."
            Log.w(TAG, errorMsg)
            return@withContext SupabaseConnectionResult.Failure(errorMsg)
        }

        val startTime = System.currentTimeMillis()
        try {
            val rootUrl = supabaseUrl.trimEnd('/')
            val request = Request.Builder()
                .url("$rootUrl/rest/v1/")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val statusCode = response.code

            if (response.isSuccessful) {
                Log.d(TAG, "Supabase connection verified successfully in ${latency}ms (HTTP $statusCode)")
                SupabaseConnectionResult.Success(
                    message = "اتصال با سوپابیس فعال و پایدار است.",
                    statusCode = statusCode,
                    latencyMs = latency
                )
            } else {
                val errorBody = response.body?.string()?.take(200) ?: ""
                Log.w(TAG, "Supabase connection returned HTTP $statusCode: $errorBody")
                SupabaseConnectionResult.Failure(
                    message = "پاسخ ناموفق از سرور سوپابیس (کد $statusCode)",
                    statusCode = statusCode
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Supabase: ${e.message}", e)
            SupabaseConnectionResult.Failure(
                message = "خطا در برقراری ارتباط با سرور: ${e.localizedMessage ?: "عدم اتصال به شبکه"}",
                error = e
            )
        }
    }

    companion object {
        private const val TAG = "SupabaseClientProvider"

        @Volatile
        private var instance: SupabaseClientProvider? = null

        /**
         * Returns the singleton instance of [SupabaseClientProvider].
         */
        fun getInstance(): SupabaseClientProvider {
            return instance ?: synchronized(this) {
                instance ?: SupabaseClientProvider().also { instance = it }
            }
        }
    }
}
