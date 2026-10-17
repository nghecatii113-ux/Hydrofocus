package com.example.data.repository

import com.example.data.dao.RewardUrlDao
import com.example.data.dao.UserSettingDao
import com.example.data.dao.WaterLogDao
import com.example.data.model.RewardUrl
import com.example.data.model.UserSetting
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HydroRepository(
    private val waterLogDao: WaterLogDao,
    private val rewardUrlDao: RewardUrlDao,
    private val userSettingDao: UserSettingDao
) {
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Water Logs
    fun getTodayLogs(): Flow<List<WaterLog>> {
        return waterLogDao.getLogsForDate(getTodayDateString())
    }

    fun getTodayTotalMl(): Flow<Int?> {
        return waterLogDao.getTotalMlForDate(getTodayDateString())
    }

    suspend fun logWater(amountMl: Int, note: String? = null) {
        val log = WaterLog(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateString = getTodayDateString(),
            note = note
        )
        waterLogDao.insertLog(log)
    }

    suspend fun deleteWaterLog(id: Int) {
        waterLogDao.deleteLogById(id)
    }

    suspend fun resetTodayWater() {
        waterLogDao.clearLogsForDate(getTodayDateString())
    }

    // Vault Rewards
    val allRewards: Flow<List<RewardUrl>> = rewardUrlDao.getAllRewards()

    suspend fun addReward(title: String, url: String, category: String = "General") {
        val reward = RewardUrl(
            title = title.trim(),
            url = url.trim(),
            category = category.trim()
        )
        rewardUrlDao.insertReward(reward)
    }

    suspend fun deleteReward(id: Int) {
        rewardUrlDao.deleteRewardById(id)
    }

    suspend fun getRandomReward(): RewardUrl? {
        val randomReward = rewardUrlDao.getRandomReward()
        if (randomReward != null) {
            rewardUrlDao.incrementTimesDrawn(randomReward.id)
        }
        return randomReward
    }

    suspend fun getRewardCount(): Int {
        return rewardUrlDao.getRewardCount()
    }

    // User Settings
    val userSettingFlow: Flow<UserSetting?> = userSettingDao.getUserSettingFlow()

    suspend fun updateDailyGoal(newGoalMl: Int) {
        val current = userSettingDao.getUserSetting() ?: UserSetting()
        userSettingDao.insertOrUpdate(current.copy(dailyGoalMl = newGoalMl))
    }

    suspend fun updateGridDimension(dimension: Int) {
        val current = userSettingDao.getUserSetting() ?: UserSetting()
        userSettingDao.insertOrUpdate(current.copy(gridDimension = dimension))
    }

    suspend fun updateCustomPhotoUri(uriString: String?) {
        val current = userSettingDao.getUserSetting() ?: UserSetting()
        userSettingDao.insertOrUpdate(current.copy(customPhotoUri = uriString))
    }

    suspend fun markRewardClaimedToday() {
        val current = userSettingDao.getUserSetting() ?: UserSetting()
        userSettingDao.insertOrUpdate(current.copy(lastClaimedRewardDate = getTodayDateString()))
    }
}
