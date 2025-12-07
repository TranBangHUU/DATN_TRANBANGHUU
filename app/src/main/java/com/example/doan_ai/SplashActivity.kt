package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth.currentUser

        if (user != null) {
            checkUserRole(user)
        } else {
            navigateToScreen(targetActivity = HomeActivity::class.java)
        }
    }

    private fun checkUserRole(user: FirebaseUser) {
        val uid = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").value?.toString()
            val finalRole = role ?: if (user.email == "admin@gmail.com") "admin" else "user"

            if (finalRole == "admin") {
                navigateToScreen(targetActivity = AdminActivity::class.java)
            } else {
                navigateToScreen(targetActivity = HomeActivity::class.java)
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi kết nối DB, vui lòng khởi động lại!", Toast.LENGTH_SHORT).show()
            navigateToScreen(targetActivity = HomeActivity::class.java)
        }
    }
    private fun navigateToScreen(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}