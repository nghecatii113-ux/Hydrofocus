package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey
    val id: Int = 1,
    val dailyGoalMl: Int = 2000,
    val gridDimension: Int = 3, // 3 for 3x3 (9 pieces), 4 for 4x4 (16 pieces)
    val customPhotoUri: String? = null,
    val lastClaimedRewardDate: String = "",
    val celebrationTriggeredDate: String = ""
)
