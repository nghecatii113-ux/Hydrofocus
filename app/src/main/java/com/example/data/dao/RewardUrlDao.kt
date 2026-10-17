package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RewardUrl
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardUrlDao {
    @Query("SELECT * FROM reward_urls ORDER BY createdAt DESC")
    fun getAllRewards(): Flow<List<RewardUrl>>

    @Query("SELECT COUNT(*) FROM reward_urls")
    suspend fun getRewardCount(): Int

    @Query("SELECT * FROM reward_urls ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomReward(): RewardUrl?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardUrl): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardUrl>)

    @Update
    suspend fun updateReward(reward: RewardUrl)

    @Query("UPDATE reward_urls SET timesDrawn = timesDrawn + 1 WHERE id = :id")
    suspend fun incrementTimesDrawn(id: Int)

    @Query("DELETE FROM reward_urls WHERE id = :id")
    suspend fun deleteRewardById(id: Int)
}
