package com.zenflow.brain.detox.di

import android.content.Context
import androidx.room.Room
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.zenflow.brain.detox.data.local.AppDatabase
import com.zenflow.brain.detox.data.repository.BackupRepository
import com.zenflow.brain.detox.data.repository.BlockedAppRepository
import com.zenflow.brain.detox.data.repository.SettingsRepository
import com.zenflow.brain.detox.data.repository.UsageRepository
import com.zenflow.brain.detox.data.repository.UserRepository
import com.zenflow.brain.detox.data.remote.ZenflowApi
import com.zenflow.brain.detox.util.GoogleDriveHelper
import com.zenflow.brain.detox.util.UsageStatsHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    val context: Context = context.applicationContext

    val api: ZenflowApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://zenflow-backend-rvmn.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZenflowApi::class.java)
    }

    val userRepository: UserRepository by lazy { UserRepository(context) }

    private val database: AppDatabase by lazy {
        // SQLCipher factory with secure key
        val factory = SupportOpenHelperFactory("zenflow_secure_key".toByteArray())
        
        Room.databaseBuilder(context, AppDatabase::class.java, "brain_detox.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    val usageStatsHelper: UsageStatsHelper by lazy { UsageStatsHelper(context) }

    val blockedAppRepository: BlockedAppRepository by lazy {
        BlockedAppRepository(database.blockedAppDao(), database.usageLogDao())
    }

    val usageRepository: UsageRepository by lazy {
        UsageRepository(database.usageLogDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    val googleDriveHelper: GoogleDriveHelper by lazy {
        GoogleDriveHelper(context)
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(
            database.settingsDao(),
            database.blockedAppDao(),
            database.usageLogDao(),
            googleDriveHelper
        )
    }
}
