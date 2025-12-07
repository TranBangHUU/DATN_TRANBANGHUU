package com.example.doan_ai

data class PlantScope(
    val plantName: String,
    val diseaseCount: Int,
    val iconResId: Int,
    val type: String,
    val diseaseGroups: List<DiseaseGroup>? = null
)