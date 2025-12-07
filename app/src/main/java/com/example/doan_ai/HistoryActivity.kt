package com.example.doan_ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.hide()
        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Ánh xạ Views
        val backButton = findViewById<ImageButton>(R.id.backButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        progressBar = findViewById(R.id.historyProgressBar)
        // Cấu hình RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        // Khởi tạo Adapter
        historyAdapter = HistoryAdapter(emptyList()) { item ->
            handleHistoryItemClick(item)
        }
        historyRecyclerView.adapter = historyAdapter

        backButton.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử.", Toast.LENGTH_LONG).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        val historyRef = database.getReference("history").child(userId)
        historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<HistoryItem>()

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(HistoryItem::class.java)
                    if (item != null) {
                        item.timestampKey = itemSnapshot.key
                        historyList.add(item)
                    }
                }

                progressBar.visibility = View.GONE
                historyAdapter.updateList(historyList)
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@HistoryActivity, "Lỗi tải lịch sử: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleHistoryItemClick(item: HistoryItem) {
        val diseaseId = item.diseaseId

        if (diseaseId != null) {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra(MainActivity.RESULT_ID_KEY, diseaseId)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Lỗi: Không có ID bệnh để tra cứu.", Toast.LENGTH_SHORT).show()
        }
    }
}