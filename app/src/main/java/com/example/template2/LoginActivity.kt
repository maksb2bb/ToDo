package com.example.template2

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth


    companion object {
        private const val TAG = "LoginActivity"
        const val PREFS_NAME = "user_prefs"
        const val THEME_KEY = "theme_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        val regTransition: TextView = findViewById(R.id.regTransition)
        val emailText: TextView = findViewById(R.id.emailEdit)
        val pswdText: TextView = findViewById(R.id.passwordEdit)
        val loginBtn: Button = findViewById(R.id.loginBtn)
        val pswdReset: TextView = findViewById(R.id.resetPswd)
        val themeSwitcher: ImageView = findViewById(R.id.themeSwitcher)

        // Проверка наличия интернет-соединения
        if (!isOnline(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
        }

        //смена темы
        themeSwitcher.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val newThemeMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newThemeMode)
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(THEME_KEY, newThemeMode).apply()
        }

        //кнопка входа
        loginBtn.setOnClickListener {
            val email = emailText.text.toString().trim()
            val password = pswdText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //кнопка сброса пароля
        pswdReset.setOnClickListener {
            val email = emailText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Unable to send reset mail", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        regTransition.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }
}
