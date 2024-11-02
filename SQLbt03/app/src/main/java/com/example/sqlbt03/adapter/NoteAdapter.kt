package com.example.sqlbt03.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sqlbt03.R
import com.example.sqlbt03.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.titleText)
        val contentPreview: TextView = view.findViewById(R.id.contentPreview)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val importantIcon: ImageView = view.findViewById(R.id.importantIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleText.text = note.title
        holder.contentPreview.text = note.content.take(50) + if (note.content.length > 50) "..." else ""
        holder.dateText.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(note.createdDate))
        holder.importantIcon.visibility = if (note.isImportant) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onNoteClick(note) }
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}