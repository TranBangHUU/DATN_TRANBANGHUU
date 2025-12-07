package com.example.doan_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupItemAdapter(
    private val groupList: List<DiseaseGroup>,
    private val onItemClick: (DiseaseGroup) -> Unit
) : RecyclerView.Adapter<GroupItemAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_group_disease, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.tvGroupName.text = group.groupName
        holder.itemView.setOnClickListener {
            onItemClick(group)
        }
    }

    override fun getItemCount(): Int = groupList.size

    fun updateList(newList: List<DiseaseGroup>) {
        (groupList as MutableList).clear()
        (groupList as MutableList).addAll(newList)
        notifyDataSetChanged()
    }
}