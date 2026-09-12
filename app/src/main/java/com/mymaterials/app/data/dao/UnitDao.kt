package com.mymaterials.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mymaterials.app.data.entity.StudyUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order` ASC")
    fun getUnitsForSubject(subjectId: Long): Flow<List<StudyUnit>>

    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order` ASC")
    suspend fun getUnitsForSubjectSync(subjectId: Long): List<StudyUnit>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getById(id: Long): StudyUnit?

    @Query("SELECT COALESCE(MAX(`order`), -1) + 1 FROM units WHERE subjectId = :subjectId")
    suspend fun getNextOrder(subjectId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unit: StudyUnit): Long

    @Update
    suspend fun update(unit: StudyUnit)

    @Update
    suspend fun updateAll(units: List<StudyUnit>)

    @Query("SELECT COALESCE(MAX(`order`), -1) FROM units WHERE subjectId = :subjectId")
    suspend fun getMaxUnitOrder(subjectId: Long): Int

    @Delete
    suspend fun delete(unit: StudyUnit)

    @Query("SELECT * FROM units")
    suspend fun getAllSync(): List<StudyUnit>

    @Query("DELETE FROM units")
    suspend fun clearAll()
}
