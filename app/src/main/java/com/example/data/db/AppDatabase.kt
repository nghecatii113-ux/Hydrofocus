package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.RewardUrlDao
import com.example.data.dao.UserSettingDao
import com.example.data.dao.WaterLogDao
import com.example.data.model.RewardUrl
import com.example.data.model.UserSetting
import com.example.data.model.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WaterLog::class, RewardUrl::class, UserSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao
    abstract fun rewardUrlDao(): RewardUrlDao
    abstract fun userSettingDao(): UserSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydrofocus_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                // Initialize default user settings
                db.userSettingDao().insertOrUpdate(
                    UserSetting(
                        id = 1,
                        dailyGoalMl = 2000,
                        gridDimension = 3,
                        customPhotoUri = null
                    )
                )

                // Populate starter vault rewards so the mystery gacha is immediately fun!
                val starterRewards = listOf(
                    RewardUrl(
                        title = "Ambient Study & Focus Lo-Fi Beats",
                        url = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
                        category = "Music"
                    ),
                    RewardUrl(
                        title = "Kurzgesagt: The Beauty of Deep Time",
                        url = "https://www.youtube.com/watch?v=2XkF1e19G10",
                        category = "Video"
                    ),
                    RewardUrl(
                        title = "Deep Focus Calming Ocean Waves Soundscape",
                        url = "https://www.youtube.com/watch?v=bn9F19Hi1Lk",
                        category = "Music"
                    ),
                    RewardUrl(
                        title = "James Clear: 30 Life Lessons from Atomic Habits",
                        url = "https://jamesclear.com/continuous-improvement",
                        category = "Article"
                    ),
                    RewardUrl(
                        title = "Astronomy Picture of the Day (NASA APOD)",
                        url = "https://apod.nasa.gov/apod/astropix.html",
                        category = "Fun"
                    )
                )
                db.rewardUrlDao().insertRewards(starterRewards)
            }
        }
    }
}
