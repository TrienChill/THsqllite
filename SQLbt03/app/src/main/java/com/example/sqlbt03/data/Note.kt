package com.example.sqlbt03.data

data class Note(
    var id: Long = 0,
    var title: String,
    var content: String,
    var createdDate: Long = System.currentTimeMillis(),
    var isImportant: Boolean = false,
    var reminderTime: Long? = null
)