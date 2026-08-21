package com.appdynamics.sampleandroidapplication.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appdynamics.eumagent.runtime.DontObfuscate
import com.appdynamics.eumagent.runtime.Instrumentation
import com.appdynamics.sampleandroidapplication.MainActivity
import com.appdynamics.sampleandroidapplication.R
import com.appdynamics.sampleandroidapplication.auth.data.AuthRepository
import com.appdynamics.sampleandroidapplication.auth.model.AuthResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

@DontObfuscate
class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepository = AuthRepository(this)

        // Skip login screen entirely if a valid session already exists
        if (authRepository.isLoggedIn()) {
            goToMain()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail         = findViewById(R.id.til_email)
        tilPassword      = findViewById(R.id.til_password)
        etEmail          = findViewById(R.id.et_email)
        etPassword       = findViewById(R.id.et_password)
        btnLogin         = findViewById(R.id.btn_login)
        progressBar      = findViewById(R.id.progress_bar)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)

        // Pre-fill last used email for convenience
        val savedEmail = authRepository.getSavedEmail()
        if (savedEmail.isNotEmpty()) {
            etEmail.setText(savedEmail)
            etPassword.requestFocus()
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email    = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString()?.trim().orEmpty()

            if (!validate(email, password)) return@setOnClickListener

            Instrumentation.leaveBreadcrumb("Login attempted for: $email")
            Instrumentation.startTimer("Login Flow")

            lifecycleScope.launch {
                setLoading(true)
                when (val result = authRepository.login(email, password)) {
                    is AuthResult.Success -> {
                        Instrumentation.stopTimer("Login Flow")
                        Instrumentation.leaveBreadcrumb("Login succeeded, navigating to MainActivity")
                        goToMain()
                    }
                    is AuthResult.Error -> {
                        setLoading(false)
                        Instrumentation.leaveBreadcrumb("Login failed: ${result.message}")
                        Snackbar.make(btnLogin, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvForgotPassword.setOnClickListener {
            Instrumentation.leaveBreadcrumb("Forgot password tapped")
            Snackbar.make(it, "Password reset email sent (mock)", Snackbar.LENGTH_SHORT).show()
        }
    }

    // ── Inline validation (same pattern as showAddOrEditDialog in MainActivity) ─

    private fun validate(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.login_error_invalid_email)
            isValid = false
        } else {
            tilEmail.error = null
        }

        if (password.length < 6) {
            tilPassword.error = getString(R.string.login_error_password_too_short)
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled     = !loading
        etEmail.isEnabled      = !loading
        etPassword.isEnabled   = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Remove LoginActivity from back stack — pressing Back won't return here
    }
}
