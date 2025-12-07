package com.example.doan_ai

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class DiseaseDetailActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    // Khai báo Views (Đã chuyển thành EditTexts)
    private lateinit var edtDiseaseName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtTreatment: EditText

    private lateinit var backButton: ImageButton
    private lateinit var btnEditDisease: Button

    private var currentDiseaseId: String? = null
    private var isEditMode: Boolean = false

    companion object {
        const val DISEASE_ID_KEY = "disease_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detail)

        supportActionBar?.hide()
        firestore = FirebaseFirestore.getInstance()

        // 1. Ánh xạ Views
        edtDiseaseName = findViewById(R.id.textViewDiseaseName)
        edtDescription = findViewById(R.id.textViewDescription)
        edtTreatment = findViewById(R.id.textViewTreatment)

        backButton = findViewById(R.id.backButton)
        btnEditDisease = findViewById(R.id.btnEditDisease)

        currentDiseaseId = intent.getStringExtra(DISEASE_ID_KEY)

        if (currentDiseaseId != null) {
            loadDiseaseDetails(currentDiseaseId!!)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh.", Toast.LENGTH_LONG).show()
            finish()
        }

        // 4. Xử lý sự kiện
        backButton.setOnClickListener { onBackPressed() }
        btnEditDisease.setOnClickListener { toggleEditMode() }
    }

    // --- LOGIC

    private fun toggleEditMode() {
        if (isEditMode) {
            saveDiseaseDetails()
        } else {
            setEditable(true)
            btnEditDisease.text = "LƯU"
        }
        isEditMode = !isEditMode
    }

    private fun setEditable(editable: Boolean) {
        edtDiseaseName.isEnabled = editable
        edtDescription.isEnabled = editable
        edtTreatment.isEnabled = editable

        val backgroundRes = if (editable) R.drawable.editable_bg else android.R.color.transparent
        edtDiseaseName.setBackgroundResource(backgroundRes)
        edtDescription.setBackgroundResource(backgroundRes)
        edtTreatment.setBackgroundResource(backgroundRes)
    }

    // --- LOGIC TẢI DỮ LIỆU ---

    private fun loadDiseaseDetails(id: String) {
        firestore.collection("diseases").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("ten")
                    val moTa = document.getString("moTa")
                    val cachChua = document.getString("cachChua")

                    // Điền dữ liệu vào EditTexts
                    edtDiseaseName.setText(name ?: "N/A")
                    edtDescription.setText(moTa ?: "N/A")
                    edtTreatment.setText(cachChua ?: "N/A")

                    // Khóa ban đầu (View Mode)
                    setEditable(false)
                    btnEditDisease.text = "SỬA"
                } else {
                    Toast.makeText(this, "Không tìm thấy dữ liệu cho ID: $id", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DiseaseDetail", "Lỗi tải chi tiết bệnh: $id", e)
                Toast.makeText(this, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --- LOGIC LƯU DỮ LIỆU ---

    private fun saveDiseaseDetails() {
        if (currentDiseaseId == null) return

        // 1. Lấy dữ liệu mới từ EditTexts
        val ten = edtDiseaseName.text.toString().trim()
        val moTa = edtDescription.text.toString().trim()
        val cachChua = edtTreatment.text.toString().trim()

        if (ten.isEmpty() || moTa.isEmpty() || cachChua.isEmpty()) {
            Toast.makeText(this, "Nội dung không được để trống.", Toast.LENGTH_SHORT).show()
            return
        }

        val diseaseData = mapOf(
            "ten" to ten,
            "moTa" to moTa,
            "cachChua" to cachChua
        )

        firestore.collection("diseases").document(currentDiseaseId!!).set(diseaseData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                setEditable(false)
                btnEditDisease.text = "SỬA"
                isEditMode = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                setEditable(true)
                isEditMode = true
            }
    }
}