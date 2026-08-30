package com.mymaterials.app.util

import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.StudyUnit

data class BulkParsedResult(
    val units: List<StudyUnit>,
    val lessons: List<Lesson>
)

object BulkParser {

    /**
     * صياغة معتمدة:
     * - سطر يبدأ بـ * -> وحدة جديدة
     * - سطر يساوي - -> إغلاق الوحدة الحالية
     * - أي سطر آخر -> درس (داخل الوحدة لو مفتوحة، وإلا مباشر)
     * يدعم الفصل بـ \n و . كفاصل
     */
    fun parse(
        input: String,
        subjectId: Long,
        startUnitOrder: Int,
        startLessonOrder: Int
    ): BulkParsedResult {
        // ندعم . و \n كفواصل - نحول . إلى \n أولا
        val normalized = input.replace(".", "\n")
        val lines = normalized.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val units = mutableListOf<StudyUnit>()
        val lessons = mutableListOf<Lesson>()
        var currentUnitIndex: Int? = null
        var unitOrder = startUnitOrder
        // نحتاج order منفصل للدروس داخل كل وحدة vs المباشرة - سنستخدم order عام للمباشر و order خاص لكل وحدة
        val unitLessonCounters = mutableMapOf<Int, Int>()
        var directLessonOrder = startLessonOrder

        for (raw in lines) {
            when {
                raw.startsWith("*") -> {
                    val name = raw.removePrefix("*").trim()
                    if (name.isNotEmpty()) {
                        val unit = StudyUnit(
                            subjectId = subjectId,
                            name = name,
                            order = unitOrder++
                        )
                        units.add(unit)
                        currentUnitIndex = units.size - 1
                        unitLessonCounters[currentUnitIndex] = 0
                    }
                }
                raw == "-" -> {
                    currentUnitIndex = null
                }
                else -> {
                    if (currentUnitIndex != null) {
                        val idx = currentUnitIndex!!
                        val order = unitLessonCounters[idx] ?: 0
                        // نحتاج unitId مؤقت - سنستخدم index كـ placeholder ونستبدله بعد الإدخال في Repository
                        // هنا نضع unitId = - (idx+1) كعلامة مؤقتة
                        lessons.add(
                            Lesson(
                                subjectId = subjectId,
                                unitId = - (idx + 1).toLong(),
                                title = raw,
                                order = order
                            )
                        )
                        unitLessonCounters[idx] = order + 1
                    } else {
                        lessons.add(
                            Lesson(
                                subjectId = subjectId,
                                unitId = null,
                                title = raw,
                                order = directLessonOrder++
                            )
                        )
                    }
                }
            }
        }
        return BulkParsedResult(units, lessons)
    }
}
