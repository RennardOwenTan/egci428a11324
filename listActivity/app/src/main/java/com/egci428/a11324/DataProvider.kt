package com.egci428.a11324

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object DataProvider {
    private val data = ArrayList<FortuneCookie>()
    private const val FILENAME = "fortunes.txt"
    private val lock = Any() // For thread safety

    fun getData(): List<FortuneCookie> = synchronized(lock) { ArrayList(data) }

    fun addFortune(cookie: FortuneCookie, context: Context) = synchronized(lock) {
        data.add(0, cookie)
        saveToFile(context)
    }

    fun deleteItem(position: Int, context: Context): Boolean {
        if (position in data.indices) {
            data.removeAt(position)
            saveToFile(context)
            return true
        }
        return false
    }

    fun loadFromFile(context: Context) = synchronized(lock) {
        try {
            val file = File(context.filesDir, FILENAME)
            if (file.exists()) {
                val newData = file.readLines().mapNotNull { line ->
                    line.split("|").takeIf { it.size == 3 }?.let {
                        FortuneCookie(it[0], it[1], it[2])
                    }
                }
                data.clear()
                data.addAll(newData)
            }
        } catch (e: Exception) {
            Log.e("DataProvider", "Error loading file", e)
        }
    }

    private fun saveToFile(context: Context) = synchronized(lock) {
        try {
            File(context.filesDir, FILENAME).writeText(
                data.joinToString("\n") { "${it.message}|${it.time}|${it.status}" }
            )
        } catch (e: Exception) {
            Log.e("DataProvider", "Error saving file", e)
        }
    }
}