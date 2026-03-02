package com.evolvarc.adskipper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val AUTO_MUTE_ADS_KEY = booleanPreferencesKey("auto_mute_ads")
        val VIBRATE_ON_SKIP_KEY = booleanPreferencesKey("vibrate_on_skip")
        val SHOW_NOTIFICATION_KEY = booleanPreferencesKey("show_notification")
        val TOTAL_ADS_SKIPPED_KEY = intPreferencesKey("total_ads_skipped")
        val TOTAL_TIME_SAVED_KEY = longPreferencesKey("total_time_saved")
        val SKIP_DELAY_KEY = intPreferencesKey("skip_delay")
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
        val IS_FIRST_HOME_SCREEN_VISIT_KEY = booleanPreferencesKey("is_first_home_screen_visit")
        val SELECTED_LANGUAGE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("selected_language")
        val APP_OPEN_COUNT_KEY = intPreferencesKey("app_open_count")
    }

    val appOpenCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[APP_OPEN_COUNT_KEY] ?: 0
    }

    suspend fun incrementAppOpenCount() {
        dataStore.edit {
            val currentCount = it[APP_OPEN_COUNT_KEY] ?: 0
            it[APP_OPEN_COUNT_KEY] = currentCount + 1
        }
    }

    val autoMuteAds: Flow<Boolean> = dataStore.data.map {
        it[AUTO_MUTE_ADS_KEY] ?: true // Default to true
    }

    suspend fun setAutoMuteAds(isAutoMute: Boolean) {
        dataStore.edit {
            it[AUTO_MUTE_ADS_KEY] = isAutoMute
        }
    }

    val vibrateOnSkip: Flow<Boolean> = dataStore.data.map {
        it[VIBRATE_ON_SKIP_KEY] ?: false // Default to false
    }

    suspend fun setVibrateOnSkip(isVibrate: Boolean) {
        dataStore.edit {
            it[VIBRATE_ON_SKIP_KEY] = isVibrate
        }
    }

    val showNotification: Flow<Boolean> = dataStore.data.map {
        it[SHOW_NOTIFICATION_KEY] ?: true // Default to true
    }

    suspend fun setShowNotification(isShow: Boolean) {
        dataStore.edit {
            it[SHOW_NOTIFICATION_KEY] = isShow
        }
    }
    
    val totalAdsSkipped: Flow<Int> = dataStore.data.map { preferences ->
        preferences[TOTAL_ADS_SKIPPED_KEY] ?: 0
    }

    suspend fun incrementTotalAdsSkipped() {
        dataStore.edit {
            val currentCount = it[TOTAL_ADS_SKIPPED_KEY] ?: 0
            it[TOTAL_ADS_SKIPPED_KEY] = currentCount + 1
        }
    }

    val totalTimeSaved: Flow<Long> = dataStore.data.map { preferences ->
        preferences[TOTAL_TIME_SAVED_KEY] ?: 0L
    }

    suspend fun addTimeSaved(seconds: Long) {
        dataStore.edit {
            val currentTimeSaved = it[TOTAL_TIME_SAVED_KEY] ?: 0L
            it[TOTAL_TIME_SAVED_KEY] = currentTimeSaved + seconds
        }
    }

    suspend fun resetStatistics() {
        dataStore.edit {
            it[TOTAL_ADS_SKIPPED_KEY] = 0
            it[TOTAL_TIME_SAVED_KEY] = 0L
        }
    }

    val skipDelay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SKIP_DELAY_KEY] ?: 0 // Default to 0 (0.1s)
    }

    suspend fun setSkipDelay(delay: Int) {
        dataStore.edit {
            it[SKIP_DELAY_KEY] = delay
        }
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETE_KEY] ?: false
    }

    suspend fun setOnboardingComplete(isComplete: Boolean) {
        dataStore.edit {
            it[ONBOARDING_COMPLETE_KEY] = isComplete
        }
    }

    val isFirstHomeScreenVisit: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_FIRST_HOME_SCREEN_VISIT_KEY] ?: true
    }

    suspend fun setFirstHomeScreenVisit(isFirst: Boolean) {
        dataStore.edit {
            it[IS_FIRST_HOME_SCREEN_VISIT_KEY] = isFirst
        }
    }



    val selectedLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE_KEY] ?: "ALL"
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        dataStore.edit {
            it[SELECTED_LANGUAGE_KEY] = languageCode
        }
    }
}
