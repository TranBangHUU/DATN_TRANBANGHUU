package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var rvDiseaseGroups: RecyclerView
    private lateinit var tvPlantTitle: TextView
    private lateinit var backButton: ImageButton

    companion object {
        const val PLANT_NAME_KEY = "PLANT_NAME"
        const val DISEASE_GROUPS_JSON_KEY = "DISEASE_GROUPS_JSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        supportActionBar?.hide()

        // Ánh xạ Views
        rvDiseaseGroups = findViewById(R.id.rvDiseaseGroups)
        tvPlantTitle = findViewById(R.id.tvPlantTitle)
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        val plantName = intent.getStringExtra(PLANT_NAME_KEY) ?: "Chi tiết Bệnh"
        val groupsJson = intent.getStringExtra(DISEASE_GROUPS_JSON_KEY)

        tvPlantTitle.text = "Nhóm bệnh: ${plantName}"

        if (groupsJson != null) {
            try {
                val type = object : TypeToken<List<DiseaseGroup>>() {}.type
                val diseaseGroups: List<DiseaseGroup> = Gson().fromJson(groupsJson, type)

                setupRecyclerView(diseaseGroups)
            } catch (e: Exception) {
                Log.e("GroupDetail", "Lỗi phân tích JSON nhóm bệnh: $e")
                Toast.makeText(this, "Lỗi tải dữ liệu nhóm bệnh.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu nhóm bệnh.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView(groups: List<DiseaseGroup>) {
        rvDiseaseGroups.layoutManager = LinearLayoutManager(this)

        val adapter = GroupItemAdapter(groups) { group ->
            handleGroupItemClick(group)
        }
        rvDiseaseGroups.adapter = adapter
    }

    // --- XỬ LÝ NHẤN ---

    private fun handleGroupItemClick(group: DiseaseGroup) {
        val selectedDiseaseId = group.diseaseId

        if (selectedDiseaseId.isNullOrEmpty()) {
            Toast.makeText(this, "Nhóm bệnh này chưa có ID tra cứu.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(MainActivity.RESULT_ID_KEY, selectedDiseaseId)
        }
        startActivity(intent)
    }
}