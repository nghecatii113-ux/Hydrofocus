package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.RewardUrl
import com.example.data.model.UserSetting
import com.example.data.model.WaterLog
import com.example.data.repository.HydroRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HydroUiState(
    val intakeMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val progress: Float = 0f,
    val gridDimension: Int = 3, // 3 or 4
    val unlockedCount: Int = 0,
    val totalPieces: Int = 9,
    val customPhotoUri: String? = null,
    val todayLogs: List<WaterLog> = emptyList(),
    val rewardList: List<RewardUrl> = emptyList(),
    val isGoalAchieved: Boolean = false,
    val isRewardClaimedToday: Boolean = false,
    val showConfetti: Boolean = false,
    val activeReward: RewardUrl? = null,
    val isGachaModalVisible: Boolean = false,
    val toastMessage: String? = null
)

private data class TransientUiState(
    val showConfetti: Boolean = false,
    val activeReward: RewardUrl? = null,
    val isGachaModalVisible: Boolean = false,
    val toastMessage: String? = null
)

class HydroViewModel(
    application: Application,
    private val repository: HydroRepository
) : AndroidViewModel(application) {

    private val _transientState = MutableStateFlow(TransientUiState())
    private var previousIntakeMl: Int = 0

    // Combine the 4 repository flows
    private val repoFlow = combine(
        repository.getTodayTotalMl(),
        repository.userSettingFlow,
        repository.getTodayLogs(),
        repository.allRewards
    ) { rawIntake, userSetting, logs, rewards ->
        val intake = rawIntake ?: 0
        val setting = userSetting ?: UserSetting()
        RepoBundle(intake, setting, logs, rewards)
    }

    val uiState: StateFlow<HydroUiState> = combine(
        repoFlow,
        _transientState
    ) { repo, transient ->
        val intake = repo.intake
        val setting = repo.setting
        val goal = if (setting.dailyGoalMl > 0) setting.dailyGoalMl else 2000
        val progress = (intake.toFloat() / goal.toFloat()).coerceAtLeast(0f)
        val dimension = if (setting.gridDimension in 3..4) setting.gridDimension else 3
        val totalPieces = dimension * dimension
        val unlockedCount = if (progress >= 1.0f) {
            totalPieces
        } else {
            (progress * totalPieces).toInt().coerceIn(0, totalPieces)
        }
        val isGoalAchieved = intake >= goal
        val todayStr = repository.getTodayDateString()
        val isClaimed = setting.lastClaimedRewardDate == todayStr

        // Trigger confetti when user hits the goal
        if (intake >= goal && previousIntakeMl < goal && previousIntakeMl > 0) {
            _transientState.update { it.copy(showConfetti = true) }
        }
        previousIntakeMl = intake

        HydroUiState(
            intakeMl = intake,
            dailyGoalMl = goal,
            progress = progress,
            gridDimension = dimension,
            unlockedCount = unlockedCount,
            totalPieces = totalPieces,
            customPhotoUri = setting.customPhotoUri,
            todayLogs = repo.logs,
            rewardList = repo.rewards,
            isGoalAchieved = isGoalAchieved,
            isRewardClaimedToday = isClaimed,
            showConfetti = transient.showConfetti,
            activeReward = transient.activeReward,
            isGachaModalVisible = transient.isGachaModalVisible,
            toastMessage = transient.toastMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HydroUiState()
    )

    fun logWater(amountMl: Int, note: String? = null) {
        viewModelScope.launch {
            repository.logWater(amountMl, note)
            if (uiState.value.intakeMl + amountMl >= uiState.value.dailyGoalMl) {
                _transientState.update { it.copy(showConfetti = true) }
            }
        }
    }

    fun deleteWaterLog(id: Int) {
        viewModelScope.launch {
            repository.deleteWaterLog(id)
        }
    }

    fun resetToday() {
        viewModelScope.launch {
            repository.resetTodayWater()
            _transientState.update { it.copy(showConfetti = false) }
        }
    }

    fun setDailyGoal(newGoal: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(newGoal)
        }
    }

    fun setGridDimension(dimension: Int) {
        viewModelScope.launch {
            repository.updateGridDimension(dimension)
        }
    }

    fun setCustomPhoto(uri: String?) {
        viewModelScope.launch {
            repository.updateCustomPhotoUri(uri)
            _transientState.update {
                it.copy(toastMessage = if (uri != null) "Focus Motivation Photo updated!" else "Photo reset to default")
            }
        }
    }

    fun addReward(title: String, url: String, category: String) {
        viewModelScope.launch {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            repository.addReward(title, formattedUrl, category)
            _transientState.update { it.copy(toastMessage = "Reward added to Mystery Vault!") }
        }
    }

    fun deleteReward(id: Int) {
        viewModelScope.launch {
            repository.deleteReward(id)
        }
    }

    fun claimDailyMysteryReward() {
        viewModelScope.launch {
            val reward = repository.getRandomReward()
            val finalReward = reward ?: RewardUrl(
                id = 0,
                title = "Relaxing Lofi Beats & Ocean Breeze",
                url = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
                category = "Music"
            )
            repository.markRewardClaimedToday()
            _transientState.update {
                it.copy(
                    activeReward = finalReward,
                    isGachaModalVisible = true,
                    showConfetti = true
                )
            }
        }
    }

    fun dismissGachaModal() {
        _transientState.update { it.copy(isGachaModalVisible = false) }
    }

    fun dismissConfetti() {
        _transientState.update { it.copy(showConfetti = false) }
    }

    fun clearToast() {
        _transientState.update { it.copy(toastMessage = null) }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(
                        application,
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    )
                    val repository = HydroRepository(
                        db.waterLogDao(),
                        db.rewardUrlDao(),
                        db.userSettingDao()
                    )
                    return HydroViewModel(application, repository) as T
                }
            }
    }
}

private data class RepoBundle(
    val intake: Int,
    val setting: UserSetting,
    val logs: List<WaterLog>,
    val rewards: List<RewardUrl>
)
