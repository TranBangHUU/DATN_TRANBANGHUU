package com.example.doan_ai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class ResultActivity : AppCompatActivity() {

    private lateinit var textViewDiseaseName: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var textViewTreatment: TextView
    private lateinit var backButton: ImageButton
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Ẩn ActionBar mặc định nếu có
        supportActionBar?.hide()

        // Ánh xạ các View
        textViewDiseaseName = findViewById(R.id.textViewDiseaseName)
        textViewDescription = findViewById(R.id.textViewDescription)
        textViewTreatment = findViewById(R.id.textViewTreatment)
        backButton = findViewById(R.id.backButton)

        // Khởi tạo Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Xử lý nút quay lại
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Lấy ID từ Intent
        val diseaseId = intent.getStringExtra(MainActivity.RESULT_ID_KEY)

        if (diseaseId != null) {
            // Truy vấn Firebase Firestore với ID nhận được
            db.collection("diseases").document(diseaseId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("ten")
                        val description = documentSnapshot.getString("moTa")
                        val treatment = documentSnapshot.getString("cachChua")

                        textViewDiseaseName.text = name ?: "N/A (Tên)"
                        textViewDescription.text = description ?: "N/A (Mô tả)"
                        textViewTreatment.text = treatment ?: "N/A (Cách chữa)"

                        Log.d("Firestore", "Dữ liệu tên: $name")
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu cho ID: $diseaseId", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi khi truy vấn dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Không nhận được ID bệnh để tra cứu.", Toast.LENGTH_LONG).show()
        }
    }
}