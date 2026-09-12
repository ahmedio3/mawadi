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
        val all = subjectDao.getAllSync()
        val updated = if (subject.isPinned) {
            // إلغاء التثبيت: تنزل لنهاية القائمة العادية
            val maxOrder = all.maxOfOrNull { it.order } ?: 0
            subject.copy(isPinned = false, pinnedAt = null, order = maxOrder + 1)
        } else {
            // التثبيت: تصعد لأعلى القائمة المثبتة
            val minPinned = all.filter { it.isPinned }.minOfOrNull { it.order } ?: 0
            subject.copy(isPinned = true, pinnedAt = System.currentTimeMillis(), order = minPinned - 1)
        }
        subjectDao.update(updated)
        return updated
    }

    // إعادة ترتيب المواد: ترقيم مستقل داخل كل مجموعة (مثبت / عادي)
    suspend fun reorderSubjects(ordered: List<Subject>) {
        val pinned = ordered.filter { it.isPinned }.mapIndexed { i, s -> s.copy(order = i) }
        val normal = ordered.filter { !it.isPinned }.mapIndexed { i, s -> s.copy(order = i) }
        subjectDao.updateAll(pinned + normal)
    }

    // Units
    fun getUnitsForSubject(subjectId: Long): Flow<List<StudyUnit>> = unitDao.getUnitsForSubject(subjectId)
    suspend fun getUnitsForSubjectSync(subjectId: Long) = unitDao.getUnitsForSubjectSync(subjectId)

    // الوحدات والدروس المباشرة تتشارك عدّاد ترتيب واحد داخل المادة
    // حتى يبقى ترتيبها المختلط ثابتا وقابلا لإعادة الترتيب
    private suspend fun nextTopLevelOrder(subjectId: Long): Int {
        return maxOf(
            unitDao.getMaxUnitOrder(subjectId),
            lessonDao.getMaxDirectLessonOrder(subjectId)
        ) + 1
    }

    suspend fun addUnit(subjectId: Long, name: String): Long {
        val order = nextTopLevelOrder(subjectId)
        return unitDao.insert(StudyUnit(subjectId = subjectId, name = name.trim(), order = order))
    }

    suspend fun updateUnit(unit: StudyUnit) = unitDao.update(unit)
    suspend fun deleteUnit(unit: StudyUnit) = unitDao.delete(unit)
    suspend fun toggleUnitExpanded(unit: StudyUnit) = unitDao.update(unit.copy(isExpanded = !unit.isExpanded))

    // Lessons
    fun getLessonsForSubject(subjectId: Long): Flow<List<Lesson>> = lessonDao.getLessonsForSubject(subjectId)

    suspend fun addLessonDirect(subjectId: Long, title: String): Long {
        val order = nextTopLevelOrder(subjectId)
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

    // عنصر علوي: وحدة أو درس مباشر (يتشاركان نفس مساحة الترتيب)
    sealed interface TopLevelItem {
        data class UnitItem(val unit: StudyUnit) : TopLevelItem
        data class LessonItem(val lesson: Lesson) : TopLevelItem
    }

    // حفظ الترتيب المختلط الجديد للوحدات والدروس المباشرة
    suspend fun reorderTopLevel(items: List<TopLevelItem>) {
        val newUnits = mutableListOf<StudyUnit>()
        val newLessons = mutableListOf<Lesson>()
        items.forEachIndexed { index, item ->
            when (item) {
                is TopLevelItem.UnitItem -> newUnits.add(item.unit.copy(order = index))
                is TopLevelItem.LessonItem -> newLessons.add(item.lesson.copy(order = index))
            }
        }
        if (newUnits.isNotEmpty()) unitDao.updateAll(newUnits)
        if (newLessons.isNotEmpty()) lessonDao.updateAll(newLessons)
    }

    // حفظ ترتيب الدروس داخل وحدة واحدة
    suspend fun reorderUnitLessons(lessons: List<Lesson>) {
        if (lessons.isEmpty()) return
        lessonDao.updateAll(lessons.mapIndexed { index, lesson -> lesson.copy(order = index) })
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
