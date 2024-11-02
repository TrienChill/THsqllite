package com.example.sqlbt03

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sqlbt03.data.DatabaseHelper
import com.example.sqlbt03.data.Note
import com.example.sqlbt03.databinding.ActivityNoteDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var dbHelper: DatabaseHelper
    private var noteId: Long = -1
    private var currentNote: Note? = null
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        noteId = intent.getLongExtra("note_id", -1)

        setupToolbar()
        setupButtons()

        if (noteId != -1L) {
            loadNote()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (noteId != -1L) "Edit Note" else "New Note"
    }

    private fun loadNote() {
        currentNote = dbHelper.getNote(noteId)
        currentNote?.let { note ->
            binding.titleEditText.setText(note.title)
            binding.contentEditText.setText(note.content)
            binding.importantCheckBox.isChecked = note.isImportant

            note.reminderTime?.let { reminderTime ->
                calendar.timeInMillis = reminderTime
                updateReminderButtonText()
            }
        }
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            saveNote()
        }

        binding.setReminderButton.setOnClickListener {
            showDateTimePicker()
        }

        binding.deleteReminderButton.setOnClickListener {
            calendar.clear()
            updateReminderButtonText()
        }
    }

    private fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                        updateReminderButtonText()
                    },
                    currentDateTime.get(Calendar.HOUR_OF_DAY),
                    currentDateTime.get(Calendar.MINUTE),
                    false
                ).show()
            },
            currentDateTime.get(Calendar.YEAR),
            currentDateTime.get(Calendar.MONTH),
            currentDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateReminderButtonText() {
        if (calendar.timeInMillis == 0L) {
            binding.setReminderButton.text = "Set Reminder"
            binding.deleteReminderButton.visibility = View.GONE
        } else {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.setReminderButton.text = "Reminder: ${dateFormat.format(calendar.time)}"
            binding.deleteReminderButton.visibility = View.VISIBLE
        }
    }

    private fun saveNote() {
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleEditText.error = "Title cannot be empty"
            return
        }

        val note = Note(
            id = noteId,
            title = title,
            content = content,
            isImportant = binding.importantCheckBox.isChecked,
            reminderTime = if (calendar.timeInMillis == 0L) null else calendar.timeInMillis,
            createdDate = currentNote?.createdDate ?: System.currentTimeMillis()
        )

        if (noteId == -1L) {
            noteId = dbHelper.addNote(note)
        } else {
            dbHelper.updateNote(note)
        }

        // Schedule or update reminder if set
        note.reminderTime?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                scheduleReminder(noteId, title, reminderTime)
            }
        }

        finish()
    }

    private fun scheduleReminder(noteId: Long, title: String, reminderTime: Long) {
        val workManager = WorkManager.getInstance(applicationContext)

        // Cancel any existing reminder for this note
        workManager.cancelAllWorkByTag("reminder_$noteId")

        // Create reminder notification work request
        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workDataOf(
                "note_id" to noteId,
                "note_title" to title
            ))
            .setInitialDelay(
                reminderTime - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .addTag("reminder_$noteId")
            .build()

        workManager.enqueue(reminderRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (noteId != -1L) {
            menuInflater.inflate(R.menu.menu_note_detail, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.action_share -> {
                shareNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNote()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote() {
        if (noteId != -1L) {
            dbHelper.deleteNote(noteId)
            // Cancel any scheduled reminder
            WorkManager.getInstance(applicationContext)
                .cancelAllWorkByTag("reminder_$noteId")
            finish()
        }
    }

    private fun shareNote() {
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()
        val shareText = "$title\n\n$content"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Share Note"))
    }
}
