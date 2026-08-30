package com.mymaterials.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mymaterials.app.data.entity.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY isPinned DESC, pinnedAt DESC, `order` ASC, id ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectById(id: Long): Flow<Subject?>

    @Query("SELECT COALESCE(MAX(`order`), -1) + 1 FROM subjects")
    suspend fun getNextOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("DELETE FROM subjects")
    suspend fun clearAll()

    @Query("SELECT * FROM subjects")
    suspend fun getAllSync(): List<Subject>
}
