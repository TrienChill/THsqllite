package com.example.sqlbt03.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "Note.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NOTES = "notes"
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_CREATED_DATE = "created_date"
        private const val KEY_IS_IMPORTANT = "is_important"
        private const val KEY_REMINDER_TIME = "reminder_time"
    }

    //khởi tạo lấy dữ liệu từ assets
    private val context: Context = context

    init {
        // Kiểm tra và sao chép cơ sở dữ liệu từ assets nếu cần
        if (!isDatabaseExists()) {
            copyDatabase()
        }
    }

    private fun isDatabaseExists(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    private fun copyDatabase() {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val inputStream: InputStream = context.assets.open(DATABASE_NAME)
        val outputStream: OutputStream = FileOutputStream(dbFile)

        try {
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        //bỏ qua việc tạo bản tránh phát sinh lỗi
//        val createTable = """
//            CREATE TABLE $TABLE_NOTES (
//                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                $KEY_TITLE TEXT,
//                $KEY_CONTENT TEXT,
//                $KEY_CREATED_DATE INTEGER,
//                $KEY_IS_IMPORTANT INTEGER,
//                $KEY_REMINDER_TIME INTEGER
//            )
//        """.trimIndent()
//        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    fun addNote(note: Note): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, note.title)
            put(KEY_CONTENT, note.content)
            put(KEY_CREATED_DATE, note.createdDate)
            put(KEY_IS_IMPORTANT, if (note.isImportant) 1 else 0)
            put(KEY_REMINDER_TIME, note.reminderTime)
        }
        return db.insert(TABLE_NOTES, null, values)
    }

    fun updateNote(note: Note): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, note.title)
            put(KEY_CONTENT, note.content)
            put(KEY_IS_IMPORTANT, if (note.isImportant) 1 else 0)
            put(KEY_REMINDER_TIME, note.reminderTime)
        }
        return db.update(TABLE_NOTES, values, "$KEY_ID = ?", arrayOf(note.id.toString()))
    }

    fun deleteNote(noteId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NOTES, "$KEY_ID = ?", arrayOf(noteId.toString()))
    }

    fun getNote(id: Long): Note? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$KEY_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Note(
                    id = it.getLong(it.getColumnIndexOrThrow(KEY_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(KEY_TITLE)),
                    content = it.getString(it.getColumnIndexOrThrow(KEY_CONTENT)),
                    createdDate = it.getLong(it.getColumnIndexOrThrow(KEY_CREATED_DATE)),
                    isImportant = it.getInt(it.getColumnIndexOrThrow(KEY_IS_IMPORTANT)) == 1,
                    reminderTime = it.getLong(it.getColumnIndexOrThrow(KEY_REMINDER_TIME))
                )
            } else null
        }
    }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NOTES ORDER BY $KEY_CREATED_DATE DESC"

        db.rawQuery(selectQuery, null).use { cursor ->
            while (cursor.moveToNext()) {
                notes.add(
                    Note(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                        content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT)),
                        createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED_DATE)),
                        isImportant = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_IMPORTANT)) == 1,
                        reminderTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER_TIME))
                    )
                )
            }
        }
        return notes
    }
}