package com.example.sqlbt02

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "ContactsDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CONTACTS = "contacts"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT,
                $KEY_PHONE TEXT,
                $KEY_EMAIL TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun addContact(contact: Contact): Long {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(KEY_NAME, contact.name)
                put(KEY_PHONE, contact.phone)
                put(KEY_EMAIL, contact.email)
            }
            db.insert(TABLE_CONTACTS, null, values)
        }
    }

    fun getContact(id: Int): Contact {
        return readableDatabase.use { db ->
            val cursor = db.query(
                TABLE_CONTACTS,
                null,
                "$KEY_ID = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    Contact(
                        id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(KEY_NAME)),
                        phone = it.getString(it.getColumnIndexOrThrow(KEY_PHONE)),
                        email = it.getString(it.getColumnIndexOrThrow(KEY_EMAIL))
                    )
                } else {
                    throw IllegalStateException("Contact not found")
                }
            }
        }
    }

    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT * FROM $TABLE_CONTACTS", null)
            cursor.use {
                while (it.moveToNext()) {
                    contacts.add(
                        Contact(
                            id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                            name = it.getString(it.getColumnIndexOrThrow(KEY_NAME)),
                            phone = it.getString(it.getColumnIndexOrThrow(KEY_PHONE)),
                            email = it.getString(it.getColumnIndexOrThrow(KEY_EMAIL))
                        )
                    )
                }
            }
        }
        return contacts
    }

    fun updateContact(contact: Contact): Int {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(KEY_NAME, contact.name)
                put(KEY_PHONE, contact.phone)
                put(KEY_EMAIL, contact.email)
            }
            db.update(
                TABLE_CONTACTS,
                values,
                "$KEY_ID = ?",
                arrayOf(contact.id.toString())
            )
        }
    }

    fun deleteContact(contact: Contact) {
        writableDatabase.use { db ->
            db.delete(
                TABLE_CONTACTS,
                "$KEY_ID = ?",
                arrayOf(contact.id.toString())
            )
        }
    }
}