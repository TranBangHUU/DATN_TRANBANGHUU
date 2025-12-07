package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        supportActionBar?.hide()

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 1. Ánh xạ các Views
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        val cardDiagnosisHistory = findViewById<CardView>(R.id.cardDiagnosisHistory)

        backButton.setOnClickListener {
            onBackPressed()
        }
        btnSignOut.setOnClickListener {
            handleSignOut()
        }
        cardDiagnosisHistory.setOnClickListener {
            navigateToActivity(HistoryActivity::class.java)
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }
    private fun handleSignOut() {
        auth.signOut()
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()
        finish()
    }
}