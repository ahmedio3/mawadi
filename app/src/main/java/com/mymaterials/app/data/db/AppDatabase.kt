package com.mymaterials.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mymaterials.app.data.dao.LessonDao
import com.mymaterials.app.data.dao.SubjectDao
import com.mymaterials.app.data.dao.UnitDao
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.StudyUnit
import com.mymaterials.app.data.entity.Subject

@Database(
    entities = [Subject::class, StudyUnit::class, Lesson::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDao(): LessonDao
}
