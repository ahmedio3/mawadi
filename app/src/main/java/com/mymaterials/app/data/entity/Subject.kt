package com.mymaterials.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
)
