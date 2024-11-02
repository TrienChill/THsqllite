package com.example.sqlbt02

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddContactActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        db = DatabaseHelper(this)
        nameInput = findViewById(R.id.input_name)
        phoneInput = findViewById(R.id.input_phone)
        emailInput = findViewById(R.id.input_email)

        findViewById<Button>(R.id.button_save).setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val name = nameInput.text.toString()
        val phone = phoneInput.text.toString()
        val email = emailInput.text.toString()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và số điện thoại", Toast.LENGTH_SHORT).show()
            return
        }

        val contact = Contact(
            name = name,
            phone = phone,
            email = email
        )

        db.addContact(contact)
        setResult(Activity.RESULT_OK)
        finish()
    }
}
