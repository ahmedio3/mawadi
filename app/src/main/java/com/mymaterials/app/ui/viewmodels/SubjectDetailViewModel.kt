package com.mymaterials.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.StudyUnit
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.util.BulkParser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectDetailViewModel(
    private val subjectId: Long,
    private val repository: MaterialsRepository
) : ViewModel() {

    val subject: StateFlow<Subject?> = repository.getSubjectById(subjectId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val units: StateFlow<List<StudyUnit>?> = repository.getUnitsForSubject(subjectId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val lessons: StateFlow<List<Lesson>?> = repository.getLessonsForSubject(subjectId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun addUnit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addUnit(subjectId, trimmed)
        }
    }

    fun updateUnit(unit: StudyUnit, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateUnit(unit.copy(name = trimmed))
        }
    }

    fun deleteUnit(unit: StudyUnit) {
        viewModelScope.launch {
            repository.deleteUnit(unit)
        }
    }

    fun toggleUnitExpanded(unit: StudyUnit) {
        viewModelScope.launch {
            repository.toggleUnitExpanded(unit)
        }
    }

    fun addLessonDirect(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addLessonDirect(subjectId, trimmed)
        }
    }

    fun addLessonToUnit(unitId: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addLessonToUnit(subjectId, unitId, trimmed)
        }
    }

    fun updateLesson(lesson: Lesson, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateLesson(lesson.copy(title = trimmed))
        }
    }

    fun cycleLessonStatus(lesson: Lesson) {
        viewModelScope.launch {
            repository.cycleLessonStatus(lesson)
        }
    }

    fun deleteLesson(lesson: Lesson) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
        }
    }

    fun reorderTopLevel(items: List<MaterialsRepository.TopLevelItem>) {
        viewModelScope.launch {
            repository.reorderTopLevel(items)
        }
    }

    fun reorderUnitLessons(lessons: List<Lesson>) {
        viewModelScope.launch {
            repository.reorderUnitLessons(lessons)
        }
    }

    fun bulkAdd(bulkText: String, onDone: () -> Unit) {
        if (bulkText.isBlank()) return
        viewModelScope.launch {
            val currentUnits = units.value ?: emptyList()
            val currentLessons = lessons.value ?: emptyList()

            val nextUnitOrder = (currentUnits.maxOfOrNull { it.order } ?: -1) + 1
            val nextLessonOrder = (currentLessons.filter { it.unitId == null }.maxOfOrNull { it.order } ?: -1) + 1

            val parsed = BulkParser.parse(bulkText, subjectId, nextUnitOrder, nextLessonOrder)

            val unitIdMap = mutableMapOf<Long, Long>()
            for (u in parsed.units) {
                val insertedId = repository.addUnit(subjectId, u.name)
                val idx = parsed.units.indexOf(u)
                unitIdMap[-(idx + 1).toLong()] = insertedId
            }
            for (l in parsed.lessons) {
                val realUnitId = if (l.unitId != null && l.unitId!! < 0) unitIdMap[l.unitId] else null
                if (realUnitId != null) {
                    repository.addLessonToUnit(subjectId, realUnitId, l.title)
                } else {
                    repository.addLessonDirect(subjectId, l.title)
                }
            }
            onDone()
        }
    }

    class Factory(
        private val subjectId: Long,
        private val repository: MaterialsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubjectDetailViewModel::class.java)) {
                return SubjectDetailViewModel(subjectId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
