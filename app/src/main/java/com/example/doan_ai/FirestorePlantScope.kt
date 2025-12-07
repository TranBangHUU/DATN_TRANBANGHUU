package com.example.doan_ai

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirestorePlantScope(
    val type: String? = null,
    val image: String? = null,
    val sl: Int? = null,
    val diseaseGroups: List<DiseaseGroup>? = null
)