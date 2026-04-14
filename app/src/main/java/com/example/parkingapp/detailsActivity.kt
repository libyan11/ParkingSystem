package com.example.parkingapp

import android.os.Bundle
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DetailsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference

    private val spaceList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        listView = findViewById(R.id.listViewSpaces)

        val carparkId = intent.getStringExtra("carparkId")

        database = FirebaseDatabase.getInstance()
            .getReference("locations/$carparkId/spaces")

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                spaceList.clear()

                for (space in snapshot.children) {

                    val name = space.key
                    val available = space.getValue(Boolean::class.java)

                    val status = if (available == true) "Available" else "Occupied"

                    spaceList.add("$name - $status")
                }

                val adapter = ArrayAdapter(
                    this@DetailsActivity,
                    android.R.layout.simple_list_item_1,
                    spaceList
                )

                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}