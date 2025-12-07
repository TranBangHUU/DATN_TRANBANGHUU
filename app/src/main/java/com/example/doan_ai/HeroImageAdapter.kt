package com.example.doan_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HeroImageAdapter(private val imageList: List<Int>) :
    RecyclerView.Adapter<HeroImageAdapter.HeroImageViewHolder>() {

    inner class HeroImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewCarousel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_hero_image, parent, false)
        return HeroImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroImageViewHolder, position: Int) {
        holder.imageView.setImageResource(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}