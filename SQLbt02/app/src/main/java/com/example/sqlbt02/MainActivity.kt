package com.example.sqlbt02

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ContactAdapter
    private val contacts = mutableListOf<Contact>()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout//đang sửa


    private val addContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadContacts()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) //đang sửa
        swipeRefreshLayout.setOnRefreshListener {
            loadContacts()
        }

        db = DatabaseHelper(this)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_contacts)
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)

        adapter = ContactAdapter(contacts) { contact ->
            val intent = Intent(this, ContactDetailActivity::class.java).apply {
                putExtra("contact_id", contact.id)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            addContactLauncher.launch(intent)
        }

        loadContacts()
    }

    private fun loadContacts() {
        contacts.clear()
        contacts.addAll(db.getAllContacts())
        adapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }
}