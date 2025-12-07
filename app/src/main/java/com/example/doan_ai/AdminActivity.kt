package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        // Ánh xạ các View
        val btnSignOut = findViewById<Button>(R.id.btnSignOut) //
        val btnManageDiseases = findViewById<CardView>(R.id.cardManageDiseases)

        btnManageDiseases.setOnClickListener {
            navigateToActivity(ManageDiseasesActivity::class.java)
        }
        btnSignOut.setOnClickListener {
            handleSignOut()
        }
    }
    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }

    private fun handleSignOut() {
        auth.signOut()
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        Toast.makeText(this, "Đã đăng xuất tài khoản Admin!", Toast.LENGTH_SHORT).show()
        finish()
    }
}