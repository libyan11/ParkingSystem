package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var textViewLastUpdated: TextView

    private val carparkNames = ArrayList<String>()
    private val carparkSpaces = ArrayList<Int>()
    private val carparkKeys = ArrayList<String>()

    private var lastUpdatedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        listView = findViewById(R.id.listViewParking)
        textViewLastUpdated = findViewById(R.id.textViewLastUpdated)

        database = FirebaseDatabase.getInstance(
            "https://parking-1034e-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference("locations")

        loadCarParks()
    }

    private fun loadCarParks() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                carparkNames.clear()
                carparkSpaces.clear()
                carparkKeys.clear()

                for (carpark in snapshot.children) {
                    val name = carpark.child("name").getValue(String::class.java)
                    val available = carpark.child("available").getValue(Int::class.java)

                    if (name != null && available != null) {
                        carparkNames.add(name)
                        carparkSpaces.add(available)
                        carparkKeys.add(carpark.key ?: "")
                    }
                }

                val adapter = HomepageAdapter(
                    this@HomeActivity,
                    carparkNames,
                    carparkSpaces
                )

                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
                    intent.putExtra("carparkId", carparkKeys[position])
                    startActivity(intent)
                }

                lastUpdatedTime = System.currentTimeMillis()
                updateLastUpdatedText()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateLastUpdatedText() {
        val seconds = (System.currentTimeMillis() - lastUpdatedTime) / 1000

        textViewLastUpdated.text = when {
            seconds < 60 -> "Updated a moment ago"
            seconds < 120 -> "Updated 1 minute ago"
            seconds < 3600 -> "Updated ${seconds / 60} minutes ago"
            else -> "Updated over an hour ago"
        }

        handler.postDelayed({
            updateLastUpdatedText()
        }, 60000)
    }
}