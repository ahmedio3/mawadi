package com.mymaterials.app.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mymaterials.app.data.preferences.AppPreferences
import com.mymaterials.app.data.preferences.ThemeMode
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.util.BackupManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val repository: MaterialsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun exportBackup(context: Context, uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getAllForBackup()
                BackupManager.writeToUri(context, uri, data)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "فشل تصدير النسخة الاحتياطية")
            }
        }
    }

    fun restoreBackup(context: Context, uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = BackupManager.readFromUri(context, uri)
                repository.restoreFromBackup(data)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "فشل استيراد النسخة الاحتياطية")
            }
        }
    }

    class Factory(
        private val preferences: AppPreferences,
        private val repository: MaterialsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(preferences, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
