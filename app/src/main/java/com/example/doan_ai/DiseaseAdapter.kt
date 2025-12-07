package com.example.doan_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DiseaseAdapter(
    private var diseaseList: List<Disease>,
    private val onItemClick: (Disease) -> Unit,
    private val onItemLongClick: (Disease) -> Boolean
) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    inner class DiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDiseaseName: TextView = itemView.findViewById(R.id.tvDiseaseName)

        init {
            itemView.setOnClickListener {
                onItemClick(diseaseList[adapterPosition])
            }

            itemView.setOnLongClickListener {
                onItemLongClick(diseaseList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_disease, parent, false)
        return DiseaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        val disease = diseaseList[position]
        holder.tvDiseaseName.text = disease.name
    }

    override fun getItemCount(): Int = diseaseList.size

    fun updateList(newList: List<Disease>) {
        diseaseList = newList
        notifyDataSetChanged()
    }
}