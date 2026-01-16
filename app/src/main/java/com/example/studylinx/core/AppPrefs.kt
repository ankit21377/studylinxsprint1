
package com.example.studylinx.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "studding_prefs")

object AppPrefs {

    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")

    fun darkModeFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs -> prefs[KEY_DARK_MODE] ?: false }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DARK_MODE] = enabled }
    }
}