package com.mymaterials.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyUnit::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("unitId")]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val unitId: Long? = null,
    val title: String,
    val status: LessonStatus = LessonStatus.TODO,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
)
