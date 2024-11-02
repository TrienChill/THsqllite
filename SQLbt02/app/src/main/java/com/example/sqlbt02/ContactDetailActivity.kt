package com.example.sqlbt02

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContactDetailActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var contact: Contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)

        db = DatabaseHelper(this)
        val contactId = intent.getIntExtra("contact_id", -1)
        contact = db.getContact(contactId)

        // Using view binding would be better in practice
        findViewById<EditText>(R.id.input_name).setText(contact.name)
        findViewById<EditText>(R.id.input_phone).setText(contact.phone)
        findViewById<EditText>(R.id.input_email).setText(contact.email)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.button_save).setOnClickListener { updateContact() }
        findViewById<Button>(R.id.button_delete).setOnClickListener { deleteContact() }
        findViewById<Button>(R.id.button_call).setOnClickListener { makePhoneCall() }
        findViewById<Button>(R.id.button_email).setOnClickListener { sendEmail() }
    }

    private fun updateContact() {
        contact.apply {
            name = findViewById<EditText>(R.id.input_name).text.toString()
            phone = findViewById<EditText>(R.id.input_phone).text.toString()
            email = findViewById<EditText>(R.id.input_email).text.toString()
        }

        db.updateContact(contact)
        Toast.makeText(this, "Đã cập nhật liên hệ", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteContact() {
        db.deleteContact(contact)
        Toast.makeText(this, "Đã xóa liên hệ", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun makePhoneCall() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }
        startActivity(intent)
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${contact.email}")
        }
        startActivity(intent)
    }
}