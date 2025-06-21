package com.example.activizer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

import com.example.activizer.ExerciseStats





class StatsDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Exercises.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "exercise_stats"

        private const val COLUMN_ID = "id"
        private const val COLUMN_EXERCISE_NAME = "exercise_name"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_STEPS = "steps"
        private const val COLUMN_MSG = "msg"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Bu kısım genelde serverdan yüklenmiş .db için boş bırakılır
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Gerekirse tablo yapısı güncelle
    }

    fun getAllExerciseNames(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT exercise_name FROM exercise_stats", null)
        val names = mutableListOf<String>()

        cursor.use {
            while (it.moveToNext()) {
                names.add(it.getString(it.getColumnIndexOrThrow("exercise_name")))
            }
        }

        return names
    }


    // Belirli egzersize ait tüm istatistikleri al
    fun getStatsForExercise(exerciseName: String): List<ExerciseStats> {
        val statsList = mutableListOf<ExerciseStats>()
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COLUMN_DATE, $COLUMN_DURATION, $COLUMN_STEPS, $COLUMN_MSG FROM $TABLE_NAME WHERE $COLUMN_EXERCISE_NAME = ? ORDER BY $COLUMN_DATE ASC"
            ,
            arrayOf(exerciseName)
        )

        cursor.use {
            while (it.moveToNext()) {
                val date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                val steps = it.getInt(it.getColumnIndexOrThrow(COLUMN_STEPS))
                val msg = it.getFloat(it.getColumnIndexOrThrow(COLUMN_MSG))

                statsList.add(ExerciseStats(exerciseName, date, duration, steps, msg))
            }
        }

        db.close()
        return statsList
    }
}
