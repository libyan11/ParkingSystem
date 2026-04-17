package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference

    private val carparkList = ArrayList<String>()
    private val carparkKeys = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        listView = findViewById(R.id.listViewParking)

        // 🔥 Firebase reference
        database = FirebaseDatabase.getInstance(
            "https://parking-1034e-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference("locations")

        loadCarParks()
    }

    private fun loadCarParks() {

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {


                carparkList.clear()
                carparkKeys.clear()

                // Loop through all car parks
                for (carpark in snapshot.children) {

                    val name = carpark.child("name").getValue(String::class.java)
                    val available = carpark.child("available").getValue(Int::class.java)

                    if (name != null && available != null) {
                        carparkList.add("$name - Available: $available")
                        carparkKeys.add(carpark.key!!)
                    }
                    println("DATA: " + snapshot.value)
                }

                val adapter = ArrayAdapter(
                    this@HomeActivity,
                    android.R.layout.simple_list_item_1,
                    carparkList
                )

                listView.adapter = adapter


                listView.setOnItemClickListener { _, _, position, _ ->

                    val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
                    intent.putExtra("carparkId", carparkKeys[position])
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}