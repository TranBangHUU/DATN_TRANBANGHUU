package com.example.doan_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlantScopeAdapter(
    private var plantList: List<PlantScope>,
    private val onItemClick: (PlantScope) -> Unit
) : RecyclerView.Adapter<PlantScopeAdapter.PlantScopeViewHolder>() {

    inner class PlantScopeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPlantIcon: ImageView = itemView.findViewById(R.id.imgPlantIcon)
        val tvPlantName: TextView = itemView.findViewById(R.id.tvPlantName)
        val tvDiseaseCount: TextView = itemView.findViewById(R.id.tvDiseaseCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantScopeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_scope_panel, parent, false)
        return PlantScopeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantScopeViewHolder, position: Int) {
        val plant = plantList[position]
        holder.imgPlantIcon.setImageResource(plant.iconResId)
        holder.tvPlantName.text = plant.plantName
        holder.tvDiseaseCount.text = "Chẩn đoán ${plant.diseaseCount} loại bệnh"

        holder.itemView.setOnClickListener {
            onItemClick(plant)
        }
    }

    override fun getItemCount(): Int {
        return plantList.size
    }

    fun updateList(newList: List<PlantScope>) {
        plantList = newList
        notifyDataSetChanged()
    }
}