package com.example.doan_ai

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyList: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDiseaseResult: TextView = itemView.findViewById(R.id.tvDiseaseResult)
        val tvDiagnosisTime: TextView = itemView.findViewById(R.id.tvDiagnosisTime)
        val btnHistoryDetails: Button = itemView.findViewById(R.id.btnHistoryDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        holder.tvDiseaseResult.text = "Kết quả: ${item.diseaseId}"
        holder.tvDiagnosisTime.text = "Thời gian: ${item.date}"

        holder.btnHistoryDetails.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun updateList(newList: List<HistoryItem>) {
        historyList = newList.reversed()
        notifyDataSetChanged()
    }
}