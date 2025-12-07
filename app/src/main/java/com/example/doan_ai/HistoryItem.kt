package com.example.doan_ai

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class HistoryItem(
    val diseaseId: String? = null,
    val date: String? = null,
    @get:Exclude var timestampKey: String? = null
)