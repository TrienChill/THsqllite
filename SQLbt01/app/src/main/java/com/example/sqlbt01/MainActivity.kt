package com.example.sqlbt01

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var productDatabaseHelper: ProductDatabaseHelper
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        productDatabaseHelper = ProductDatabaseHelper(this)
        productRecyclerView = findViewById(R.id.productRecyclerView)

        val products = productDatabaseHelper.getAllProducts()
        productAdapter = ProductAdapter(products)

        productRecyclerView.layoutManager = LinearLayoutManager(this)
        productRecyclerView.adapter = productAdapter

        val addProductButton: Button = findViewById(R.id.addProductButton)
        addProductButton.setOnClickListener {
            productDatabaseHelper.addProduct("New Product", 10.99, "Description of the product")
                    refreshProductList()
        }
    }

    private fun refreshProductList() {
        val products = productDatabaseHelper.getAllProducts()
        productAdapter = ProductAdapter(products)
        productRecyclerView.adapter = productAdapter
    }
}