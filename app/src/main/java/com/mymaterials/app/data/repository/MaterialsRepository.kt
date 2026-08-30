package com.mymaterials.app.data.repository

import com.mymaterials.app.data.dao.LessonDao
import com.mymaterials.app.data.dao.SubjectDao
import com.mymaterials.app.data.dao.UnitDao
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.StudyUnit
import com.mymaterials.app.data.entity.Subject
import kotlinx.coroutines.flow.Flow

class MaterialsRepository(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao
) {
    // Subjects
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    fun getSubjectById(id: Long): Flow<Subject?> = subjectDao.getSubjectById(id)

    suspend fun addSubject(name: String): Long {
        val order = subjectDao.getNextOrder()
        return subjectDao.insert(Subject(name = name.trim(), order = order))
    }

    suspend fun updateSubject(subject: Subject) = subjectDao.update(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.delete(subject)

    suspend fun togglePin(subject: Subject): Subject {
        val updated = if (subject.isPinned) {
            subject.copy(isPinned = false, pinnedAt = null)
        } else {
            subject.copy(isPinned = true, pinnedAt = System.currentTimeMillis())
        }
        subjectDao.update(updated)
        return updated
    }

    // Units
    fun getUnitsForSubject(subjectId: Long): Flow<List<StudyUnit>> = unitDao.getUnitsForSubject(subjectId)
    suspend fun getUnitsForSubjectSync(subjectId: Long) = unitDao.getUnitsForSubjectSync(subjectId)

    suspend fun addUnit(subjectId: Long, name: String): Long {
        val order = unitDao.getNextOrder(subjectId)
        return unitDao.insert(StudyUnit(subjectId = subjectId, name = name.trim(), order = order))
    }

    suspend fun updateUnit(unit: StudyUnit) = unitDao.update(unit)
    suspend fun deleteUnit(unit: StudyUnit) = unitDao.delete(unit)
    suspend fun toggleUnitExpanded(unit: StudyUnit) = unitDao.update(unit.copy(isExpanded = !unit.isExpanded))

    // Lessons
    fun getLessonsForSubject(subjectId: Long): Flow<List<Lesson>> = lessonDao.getLessonsForSubject(subjectId)

    suspend fun addLessonDirect(subjectId: Long, title: String): Long {
        val order = lessonDao.getNextLessonOrder(subjectId)
        return lessonDao.insert(Lesson(subjectId = subjectId, unitId = null, title = title.trim(), order = order))
    }

    suspend fun addLessonToUnit(subjectId: Long, unitId: Long, title: String): Long {
        val order = lessonDao.getNextLessonOrderForUnit(unitId)
        return lessonDao.insert(Lesson(subjectId = subjectId, unitId = unitId, title = title.trim(), order = order))
    }

    suspend fun updateLesson(lesson: Lesson) = lessonDao.update(lesson)
    suspend fun deleteLesson(lesson: Lesson) = lessonDao.delete(lesson)

    suspend fun cycleLessonStatus(lesson: Lesson): Lesson {
        val next = when (lesson.status) {
            LessonStatus.TODO -> LessonStatus.DONE
            LessonStatus.DONE -> LessonStatus.NEEDS_REVIEW
            LessonStatus.NEEDS_REVIEW -> LessonStatus.TODO
        }
        val updated = lesson.copy(status = next)
        lessonDao.update(updated)
        return updated
    }

    // Bulk helpers
    suspend fun insertUnitsWithLessons(units: List<StudyUnit>, lessons: List<Pair<Long?, Lesson>>) {
        // units already inserted, lessons contain temp mapping - caller handles
    }

    // Backup helpers
    suspend fun getAllForBackup(): BackupData {
        return BackupData(
            subjects = subjectDao.getAllSync(),
            units = unitDao.getAllSync(),
            lessons = lessonDao.getAllSync(),
            exportedAt = System.currentTimeMillis()
        )
    }

    suspend fun restoreFromBackup(data: BackupData) {
        subjectDao.clearAll()
        // CASCADE will clear units/lessons when subjects deleted, but also clear explicitly
        unitDao.clearAll()
        lessonDao.clearAll()
        data.subjects.forEach { subjectDao.insert(it) }
        data.units.forEach { unitDao.insert(it) }
        data.lessons.forEach { lessonDao.insert(it) }
    }
}

data class BackupData(
    val subjects: List<Subject>,
    val units: List<StudyUnit>,
    val lessons: List<Lesson>,
    val exportedAt: Long
)
