package com.appdynamics.sampleandroidapplication.auth.model

import com.appdynamics.eumagent.runtime.DontObfuscate

/**
 * Result of a logout operation.
 *
 * [Success]        — server acknowledged the session invalidation AND local token was cleared.
 * [SuccessOffline] — server call failed (network error), but local token was cleared anyway.
 *                    The user is effectively logged out; the server-side token may still be
 *                    technically valid until it expires, which is acceptable for an MVP.
 */
@DontObfuscate
sealed class LogoutResult {
    object Success : LogoutResult()
    object SuccessOffline : LogoutResult()
}
