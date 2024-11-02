package com.example.sqlbt03

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqlbt03.adapter.NoteAdapter
import com.example.sqlbt03.data.DatabaseHelper
import com.example.sqlbt03.data.Note
import com.example.sqlbt03.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.widget.SearchView


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: NoteAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        setupFab()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            mutableListOf(),
            onNoteClick = { note ->
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            },
            onNoteLongClick = { note ->
                showDeleteConfirmationDialog(note)
            }
        )

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFab() {
        binding.addNoteFab.setOnClickListener {
            startActivity(Intent(this, NoteDetailActivity::class.java))
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotes(newText)
                return true
            }
        })
    }

    private fun filterNotes(query: String?) {
        val allNotes = dbHelper.getAllNotes()
        if (query.isNullOrBlank()) {
            adapter.updateNotes(allNotes)
        } else {
            val filteredNotes = allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
            }
            adapter.updateNotes(filteredNotes)
        }
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteNote(note.id)
                loadNotes()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val notes = dbHelper.getAllNotes()
        adapter.updateNotes(notes)

        binding.emptyStateLayout.visibility =
            if (notes.isEmpty()) View.VISIBLE else View.GONE
    }
}