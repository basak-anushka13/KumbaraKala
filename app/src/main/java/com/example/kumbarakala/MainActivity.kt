package com.example.kumbarakala

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

// Grid layout (2 columns)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

// 👇 ADD HERE
        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: android.view.View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(10, 10, 10, 10)
                }
            }
        )

        // 🔥 FIREBASE CODE STARTS HERE

        val db = FirebaseFirestore.getInstance()

        val list = mutableListOf<Product>()

        val adapter = ProductAdapter(list) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("name", product.name)
            intent.putExtra("image", product.image)
            intent.putExtra("benefit", product.benefit)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        db.collection("products").get().addOnSuccessListener { result ->
            for (doc in result) {

                val name = doc.getString("name") ?: ""
                val benefit = doc.getString("benefit") ?: ""

                val image = when (name) {
                    "Curd Pot" -> R.drawable.curd_pot
                    "Clay Lamp" -> R.drawable.clay_lamp
                    "Water Pot" -> R.drawable.water_pot
                    else -> R.drawable.curd_pot
                }

                list.add(Product(name, image, benefit))
            }

            adapter.notifyDataSetChanged()
        }

    }
}