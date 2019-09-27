package ru.test.itwenty

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class DataBaseHelper(context: Context, name: String) : SQLiteOpenHelper(context, name, null, 1) {

    val allWords: ArrayList<Word>
        get() {
            val arrayList = ArrayList<Word>()
            var db = this.writableDatabase
            db.delete("words", "isDeleted = ?", arrayOf(1.toString()))

            db = this.readableDatabase
            val cursor = db.rawQuery("SELECT word, translation, sourcePosition, targetPosition FROM words", null)

            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val item = Word(cursor.getString(cursor.getColumnIndex("word")),
                        cursor.getString(cursor.getColumnIndex("translation")),
                        cursor.getInt(cursor.getColumnIndex("sourcePosition")),
                        cursor.getInt(cursor.getColumnIndex("targetPosition")))
                if (!item.isEmpty) {
                    arrayList.add(item)
                }
                cursor.moveToNext()
            }

            return arrayList
        }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE words (word TEXT, translation TEXT, isDeleted INTEGER, " +
                "sourcePosition INTEGER, targetPosition INTEGER, sourceLanguage TEXT, " +
                "targetLanguage TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS words")
        onCreate(db)
    }

    fun insertWord(item: Word) {
        if (!isInDataBase(item)) {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put("word", item.word)
            contentValues.put("translation", item.translation)
            contentValues.put("isDeleted", 0)
            contentValues.put("sourcePosition", item.sourcePosition)
            contentValues.put("targetPosition", item.targetPosition)
            contentValues.put("sourceLanguage", item.sourceLanguage)
            contentValues.put("targetLanguage", item.targetLanguage)
            db.insert("words", null, contentValues)
        }
    }

    fun getLanguages(word: String, sourceLanguage: String, targetLanguage: String): IntArray {
        val languages = IntArray(2)
        val count = DatabaseUtils.queryNumEntries(this.readableDatabase, "words",
                "word = ? ", arrayOf(word))
        print(count)
        val db = this.readableDatabase
        @SuppressLint("Recycle") val cursor = db.rawQuery("SELECT sourcePosition, targetPosition FROM words WHERE " +
                " word = '" + word + "' AND sourceLanguage = '" + sourceLanguage + "' AND " +
                "targetLanguage = '" + targetLanguage + "'", null)

        cursor.moveToFirst()

        languages[0] = cursor.getInt(cursor.getColumnIndex("sourcePosition"))
        languages[1] = cursor.getInt(cursor.getColumnIndex("targetPosition"))

        return languages
    }

    fun setDeleted(item: Word) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("word", item.word)
        contentValues.put("translation", item.translation)
        contentValues.put("isDeleted", 1)
        contentValues.put("sourcePosition", item.sourcePosition)
        contentValues.put("targetPosition", item.targetPosition)
        contentValues.put("sourceLanguage", item.sourceLanguage)
        contentValues.put("targetLanguage", item.targetLanguage)
        db.update("words", contentValues, "word = ? AND sourcePosition = ? AND targetPosition = ?",
                arrayOf(item.word, item.sourcePosition.toString(), item.targetPosition.toString()))
    }

    fun deleteWord(item: Word) {
        val db = this.writableDatabase
        db.delete("words", "isDeleted = ? AND word = ? AND sourcePosition = ? AND targetPosition = ?",
                arrayOf("0", item.word, item.sourcePosition.toString(), item.targetPosition.toString()))
    }

    fun deleteAllWords() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM words")
    }

    fun isInDataBase(word: Word): Boolean {
        val count = DatabaseUtils.queryNumEntries(this.readableDatabase, "words",
                "word = ? AND sourcePosition = ? AND targetPosition = ? AND isDeleted = ?",
                arrayOf(word.word, word.sourcePosition.toString(), word.targetPosition.toString(), "0"))
        return count != 0L
    }


}
