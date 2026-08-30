package com.mymaterials.app.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mymaterials.app.data.repository.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(data: BackupData): String = gson.toJson(data)

    fun fromJson(json: String): BackupData = gson.fromJson(json, BackupData::class.java)

    suspend fun writeToUri(context: Context, uri: Uri, data: BackupData) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(toJson(data).toByteArray(Charsets.UTF_8))
        }
    }

    suspend fun readFromUri(context: Context, uri: Uri): BackupData = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val reader = BufferedReader(InputStreamReader(input))
            val json = reader.readText()
            fromJson(json)
        } ?: throw IllegalStateException("Cannot read backup file")
    }
}
