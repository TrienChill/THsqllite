package com.example.sqlbt01

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProductDatabaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "ProductDatabase.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "Products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_DESCRIPTION = "description"
    }
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """ 
            CREATE TABLE $TABLE_NAME ( 
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
            $COLUMN_NAME TEXT, 
            $COLUMN_PRICE REAL, 
            $COLUMN_DESCRIPTION TEXT 
            ) 
        """.trimIndent()
        db.execSQL(createTable)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    fun addProduct(name: String, price: Double, description: String) {
        val db = writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_DESCRIPTION, description)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val price = getDouble(getColumnIndexOrThrow(COLUMN_PRICE))
                val description =
                    getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                products.add(Product(id, name, price, description))
            }
        }
        cursor.close()
        db.close()
        return products
    }



    fun updateProduct(id: Int, name: String, price: Double, description: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_DESCRIPTION, description)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteProduct(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}