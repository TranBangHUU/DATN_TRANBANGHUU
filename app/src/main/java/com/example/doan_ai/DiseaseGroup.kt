package com.example.doan_ai

import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName

data class DiseaseGroup(
    @SerializedName("disease_id")
    val diseaseId: String? = null,
    val groupName: String? = null,
    @get:Exclude val groupKey: String? = null
)