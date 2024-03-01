package com.example.template2

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val regTransition: TextView = findViewById(R.id.regTransition)
        val emailText: TextView = findViewById(R.id.emailEdit)
        val pswdText: TextView = findViewById(R.id.passwordEdit)
        val loginBtn: Button = findViewById(R.id.loginBtn)
        val pswdReset: TextView = findViewById(R.id.resetPswd)

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.let {
                val capabilities = it.getNetworkCapabilities(connectivityManager.activeNetwork)
                capabilities?.let { caps ->
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    ) {
                        return true
                    }
                }
            }
            return false
        }
        if (!isOnline(this)) { // 'this' refers to the Context, in an Activity you can directly use 'this'
            val intent = Intent(this, NoConnections::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            if (emailText.text.toString().isEmpty() || pswdText.text.toString().isEmpty()) {
                Toast.makeText(this, "Поле с почтой или паролем не заполенно", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mAuth.signInWithEmailAndPassword(
                    emailText.text.toString(),
                    pswdText.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intentMain = Intent(this, MainActivity::class.java)
                            startActivity(intentMain)
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }

            }
        }

        pswdReset.setOnClickListener {
            Firebase.auth.sendPasswordResetEmail(emailText.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email send")
                    } else {
                        Log.w(TAG, "Email send failed", task.exception)
                    }
                }
        }

        regTransition.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}