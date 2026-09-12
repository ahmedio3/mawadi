package com.mymaterials.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mymaterials.app.data.entity.Lesson
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE subjectId = :subjectId ORDER BY `order` ASC")
    fun getLessonsForSubject(subjectId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE subjectId = :subjectId AND unitId = :unitId ORDER BY `order` ASC")
    fun getLessonsForUnit(subjectId: Long, unitId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE subjectId = :subjectId ORDER BY `order` ASC")
    suspend fun getLessonsForSubjectSync(subjectId: Long): List<Lesson>

    @Query("SELECT COALESCE(MAX(`order`), -1) + 1 FROM lessons WHERE subjectId = :subjectId")
    suspend fun getNextLessonOrder(subjectId: Long): Int

    @Query("SELECT COALESCE(MAX(`order`), -1) + 1 FROM lessons WHERE unitId = :unitId")
    suspend fun getNextLessonOrderForUnit(unitId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lesson: Lesson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<Lesson>)

    @Update
    suspend fun update(lesson: Lesson)

    @Update
    suspend fun updateAll(lessons: List<Lesson>)

    @Query("SELECT COALESCE(MAX(`order`), -1) FROM lessons WHERE subjectId = :subjectId AND unitId IS NULL")
    suspend fun getMaxDirectLessonOrder(subjectId: Long): Int

    @Delete
    suspend fun delete(lesson: Lesson)

    @Query("SELECT * FROM lessons")
    suspend fun getAllSync(): List<Lesson>

    @Query("DELETE FROM lessons")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM lessons WHERE subjectId = :subjectId")
    fun countForSubject(subjectId: Long): Flow<Int>
}
