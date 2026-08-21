package com.appdynamics.sampleandroidapplication.auth.model

import com.appdynamics.eumagent.runtime.DontObfuscate

@DontObfuscate
sealed class AuthResult {
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
