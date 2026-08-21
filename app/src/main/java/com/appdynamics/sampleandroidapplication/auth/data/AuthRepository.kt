package com.appdynamics.sampleandroidapplication.auth.data

import android.content.Context
import android.util.Log
import com.appdynamics.eumagent.runtime.DontObfuscate
import com.appdynamics.eumagent.runtime.Instrumentation
import com.appdynamics.sampleandroidapplication.auth.model.AuthResult
import com.appdynamics.sampleandroidapplication.auth.model.LogoutResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@DontObfuscate
class AuthRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client = OkHttpClient()

    companion object {
        private const val TAG = "AuthRepository"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "auth_email"

        // Mock endpoint group — swap base URL to switch environments (dev / staging / prod)
        // Using jsonplaceholder POST /posts: returns HTTP 201 + body with an "id" field,
        // which we use to synthesise a deterministic mock JWT token.
        private const val API_BASE_URL = "https://jsonplaceholder.typicode.com"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        // ── In-memory session login tracking ──────────────────────────────
        // Intentionally NOT persisted to SharedPrefs: resets on every app
        // restart, which is the correct scope for "within a session".
        @Volatile private var loginCount: Int = 0
        private val loginHistory: MutableList<String> = mutableListOf()
    }

    // ── Session helpers ────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = prefs.getString(KEY_TOKEN, null) != null

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getSavedEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    // ── POST /auth/logout ──────────────────────────────────────────────────────

    /**
     * Notifies the server to invalidate the session token, then clears local state.
     *
     * Mock strategy: DELETE jsonplaceholder /posts/1 always returns HTTP 200.
     * Swap to your real /auth/logout endpoint when ready.
     *
     * Design rule: local token is ALWAYS cleared, even on network failure,
     * so the user is never stuck in a logged-in state they cannot escape.
     */
    suspend fun logout(): LogoutResult = withContext(Dispatchers.IO) {
        val tracker = Instrumentation.beginCall(
            "com.appdynamics.sampleandroidapplication.auth.data.AuthRepository",
            "logout"
        )
        val token = getToken()
        var serverSuccess = false

        try {
            val request = Request.Builder()
                .url("$API_BASE_URL/posts/1")  // mock: real endpoint would be /auth/logout
                .delete()
                .addHeader("Authorization", "Bearer ${token.orEmpty()}")
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "logout API response code: ${response.code}")
                serverSuccess = response.isSuccessful
            }
        } catch (e: Exception) {
            // Network failure: still clear local session so user is never stuck
            Log.e(TAG, "logout network request failed — clearing local session anyway", e)
            Instrumentation.endCall(tracker, e)
        }

        // Always clear local credentials
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EMAIL)
            .apply()

        // Clear username from AppDynamics custom session data
        Instrumentation.setUserData("username", null as String?)

        /*
        // Reset in-memory session counters so the next login starts from scratch
        loginCount = 0
        loginHistory.clear()
        Instrumentation.setUserData("login_count", "0")
        Instrumentation.setUserData("login_history", null as String?)
        */
        Log.d(TAG, "Logout complete (serverSuccess=$serverSuccess), local session cleared")

        Instrumentation.endCall(tracker)
        if (serverSuccess) LogoutResult.Success else LogoutResult.SuccessOffline
    }

    // ── POST /auth/login ───────────────────────────────────────────────────────

    /**
     * Authenticates the user against the mock login endpoint.
     *
     * Mock strategy: POST to jsonplaceholder /posts (always returns HTTP 201 + body).
     * The response "id" field is used to build a deterministic mock JWT token.
     * Swap [API_BASE_URL] and path to point at a real auth service.
     */
    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val tracker = Instrumentation.beginCall(
                "com.appdynamics.sampleandroidapplication.auth.data.AuthRepository",
                "login"
            )
            try {
                val payload = JSONObject().apply {
                    put("email", email)
                    put("password", password)   // never log the real password value
                }
                val request = Request.Builder()
                    .url("$API_BASE_URL/posts")  // mock: real endpoint would be /auth/login
                    .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "login API response code: ${response.code}")

                    if (!response.isSuccessful) {
                        Instrumentation.endCall(tracker)
                        return@withContext AuthResult.Error("Server error (${response.code})")
                    }

                    // ── Build mock token from response body ──────────────────
                    val body = response.body?.string() ?: "{}"
                    val json = JSONObject(body)
                    val mockToken = "mock-jwt-${json.optInt("id", 1)}-${System.currentTimeMillis()}"

                    // Persist session (same SharedPrefs pattern as TodoRepository)
                    prefs.edit()
                        .putString(KEY_TOKEN, mockToken)
                        .putString(KEY_EMAIL, email)
                        .apply()

                    // ── Track multiple logins within a session ───────────────
                    loginCount++
                    if (!loginHistory.contains(email)) loginHistory.add(email)
                    val historyStr = loginHistory.joinToString(",")

                    // Current active user (overwrites on each login)
                    Instrumentation.setUserData("username", email)
                    // How many login calls happened this session
                    Instrumentation.setUserData("login_count", loginCount.toString())
                    // Ordered list of every distinct username used this session
                    Instrumentation.setUserData("login_history", historyStr)
                    // Aggregatable metric: count per session visible in dashboards
                    Instrumentation.reportMetric("Login Count", loginCount.toLong())
                    // Timestamped breadcrumb on the session event timeline
                    Instrumentation.leaveBreadcrumb(
                        "Login #$loginCount: $email (session users: $historyStr)"
                    )

                    Log.d(TAG, "Login #$loginCount for $email | history: $historyStr")
                    Instrumentation.endCall(tracker)
                    AuthResult.Success(mockToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "login network request failed", e)
                Instrumentation.endCall(tracker, e)
                AuthResult.Error("Network error: ${e.localizedMessage}")
            }
        }
}
