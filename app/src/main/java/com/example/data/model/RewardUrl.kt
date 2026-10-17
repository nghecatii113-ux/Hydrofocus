package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_urls")
data class RewardUrl(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "Entertainment", // Video, Music, Article, Fun
    val createdAt: Long = System.currentTimeMillis(),
    val timesDrawn: Int = 0
)
