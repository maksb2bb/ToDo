package com.example.template2


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth


class RegistrationActivity : AppCompatActivity() {
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val loginTran: TextView = findViewById(R.id.loginTran)
        val mailText: TextView = findViewById(R.id.mailText)
        val pswd: TextView = findViewById(R.id.pswdText)
        val regBtn: Button = findViewById(R.id.buttonReg)
        val themeSwitcher: ImageView = findViewById(R.id.themeSwitcher)

        themeSwitcher.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val newThemeMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newThemeMode)
            getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().putInt(LoginActivity.THEME_KEY, newThemeMode).apply()
        }

        loginTran.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        regBtn.setOnClickListener {
            if (mailText.text.toString().isEmpty() || pswd.text.toString().isEmpty()) {
                Toast.makeText(this, "Поле с почтой или паролем не заполенно", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mAuth.createUserWithEmailAndPassword(mailText.text.toString(), pswd.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intentMain = Intent(this, MainActivity::class.java)
                            startActivity(intentMain)
                        }
                    }
            }
        }

    }
}
