package com.mymaterials.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.data.repository.MaterialsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OverallStats(
    val total: Int = 0,
    val done: Int = 0,
    val progress: Float = 0f
)

class StatsViewModel(
    private val repository: MaterialsRepository
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _overallStats = MutableStateFlow(OverallStats())
    val overallStats: StateFlow<OverallStats> = _overallStats.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val data = repository.getAllForBackup()
            val total = data.lessons.size
            val done = data.lessons.count { it.status == LessonStatus.DONE }
            val progress = if (total == 0) 0f else done.toFloat() / total
            _overallStats.value = OverallStats(total = total, done = done, progress = progress)
        }
    }

    fun getLessonsForSubject(subjectId: Long): Flow<List<Lesson>> {
        return repository.getLessonsForSubject(subjectId)
    }

    class Factory(
        private val repository: MaterialsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                return StatsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
