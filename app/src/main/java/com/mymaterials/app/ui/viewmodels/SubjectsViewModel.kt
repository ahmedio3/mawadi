package com.mymaterials.app.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.data.preferences.AppPreferences
import com.mymaterials.app.data.preferences.ThemeMode
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.util.BackupManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsViewModel(
    private val repository: MaterialsRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    val subjects: StateFlow<List<Subject>?> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode

    fun addSubject(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addSubject(trimmed)
        }
    }

    fun updateSubject(subject: Subject, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateSubject(subject.copy(name = trimmed))
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun togglePin(subject: Subject) {
        viewModelScope.launch {
            repository.togglePin(subject)
        }
    }

    fun reorderSubjects(ordered: List<Subject>) {
        viewModelScope.launch {
            repository.reorderSubjects(ordered)
        }
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        preferences.toggleTheme(isCurrentlyDark)
    }

    fun exportBackup(context: Context, uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getAllForBackup()
                BackupManager.writeToUri(context, uri, data)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطأ أثناء التصدير")
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
                onError(e.message ?: "خطأ أثناء الاستيراد")
            }
        }
    }

    class Factory(
        private val repository: MaterialsRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubjectsViewModel::class.java)) {
                return SubjectsViewModel(repository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
