package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageDiseasesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var diseaseRecyclerView: RecyclerView
    private lateinit var diseaseAdapter: DiseaseAdapter
    private lateinit var btnAddDisease: Button
    private lateinit var backButton: ImageButton

    companion object {
        const val DISEASE_ID_KEY = "disease_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_diseases)

        supportActionBar?.hide()
        firestore = FirebaseFirestore.getInstance()

        // Ánh xạ Views
        diseaseRecyclerView = findViewById(R.id.diseaseRecyclerView)
        btnAddDisease = findViewById(R.id.btnAddDisease)
        backButton = findViewById(R.id.backButton)

        diseaseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Khởi tạo Adapter
        diseaseAdapter = DiseaseAdapter(
            diseaseList = emptyList(),
            onItemClick = { disease -> openDiseaseDetailForEdit(disease.id) },
            onItemLongClick = { disease ->
                confirmDelete(disease.id)
                true
            }
        )
        diseaseRecyclerView.adapter = diseaseAdapter
        backButton.setOnClickListener { onBackPressed() }
        btnAddDisease.setOnClickListener {
            showFullAddDialog()
        }
        loadDiseaseList()
    }

    override fun onResume() {
        super.onResume()
        loadDiseaseList()
    }

    // --- LOGIC HIỂN THỊ ---
    private fun loadDiseaseList() {
        firestore.collection("diseases")
            .get()
            .addOnSuccessListener { snapshot ->
                val diseaseList = mutableListOf<Disease>()

                if (snapshot.isEmpty) {
                    Toast.makeText(this, "Chưa có bệnh nào được thêm.", Toast.LENGTH_SHORT).show()
                }

                for (document in snapshot.documents) {
                    val fullId = document.id
                    val displayName = document.getString("ten") ?: fullId
                    diseaseList.add(Disease(id = fullId, name = displayName))
                }
                diseaseAdapter.updateList(diseaseList)
            }
            .addOnFailureListener { e ->
                Log.e("Admin", "Lỗi tải danh sách bệnh", e)
                Toast.makeText(this, "Lỗi tải danh sách bệnh.", Toast.LENGTH_SHORT).show()
            }
    }

    // --- LOGIC THÊM ---
    private fun showFullAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_disease, null)

        // Ánh xạ
        val edtId = dialogView.findViewById<EditText>(R.id.edtDialogId)
        val edtTen = dialogView.findViewById<EditText>(R.id.edtDialogTen)
        val edtMoTa = dialogView.findViewById<EditText>(R.id.edtDialogMoTa)
        val edtCachChua = dialogView.findViewById<EditText>(R.id.edtDialogCachChua)

        AlertDialog.Builder(this)
            .setTitle("Thêm Document Bệnh Mới")
            .setView(dialogView)
            .setPositiveButton("Lưu") { dialog, _ ->
                saveNewDisease(edtId.text.toString(), edtTen.text.toString(),
                    edtMoTa.text.toString(), edtCachChua.text.toString())
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun saveNewDisease(id: String, ten: String, moTa: String, cachChua: String) {
        val targetId = id.trim()

        if (targetId.isEmpty() || ten.isEmpty() || moTa.isEmpty() || cachChua.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tất cả các trường.", Toast.LENGTH_LONG).show()
            return
        }

        val diseaseData = mapOf(
            "id" to targetId,
            "ten" to ten,
            "moTa" to moTa,
            "cachChua" to cachChua
        )

        firestore.collection("diseases").document(targetId).set(diseaseData)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm bệnh thành công! ID: $targetId", Toast.LENGTH_LONG).show()
                loadDiseaseList()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ManageDiseases", "Lỗi thêm mới: $e")
            }
    }
    private fun openDiseaseDetailForEdit(diseaseId: String) {
        val intent = Intent(this, DiseaseDetailActivity::class.java).apply {
            putExtra(DISEASE_ID_KEY, diseaseId)
        }
        startActivity(intent)
    }

    // --- LOGIC XÓA ---
    private fun confirmDelete(diseaseId: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận Xóa Bệnh")
            .setMessage("Bạn có chắc chắn muốn xóa Document ID: $diseaseId không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                executeDelete(diseaseId)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun executeDelete(id: String) {
        firestore.collection("diseases").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa Document ID: $id thành công.", Toast.LENGTH_SHORT).show()
                loadDiseaseList()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi xóa: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}