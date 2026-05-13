package com.example.parkingapp.controller

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.R
import com.example.parkingapp.model.ParkingSlot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailsActivity : AppCompatActivity() {

    private lateinit var textViewLocationName: TextView
    private lateinit var textViewAvailable: TextView
    private lateinit var textViewFullness: TextView
    private lateinit var progressBarCapacity: ProgressBar
    private lateinit var buttonToggleSlots: Button

    private lateinit var database: FirebaseDatabase
    private val slotList = ArrayList<ParkingSlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        textViewLocationName = findViewById(R.id.textViewLocationName)
        textViewAvailable = findViewById(R.id.textViewAvailable)
        textViewFullness = findViewById(R.id.textViewFullness)
        progressBarCapacity = findViewById(R.id.progressBarCapacity)
        buttonToggleSlots = findViewById(R.id.buttonToggleSlots)

        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            finish()
        }

        val carparkId = intent.getStringExtra("carparkId")

        if (carparkId.isNullOrEmpty()) {
            finish()
            return
        }

        database = FirebaseDatabase.getInstance(
            "https://parking-1034e-default-rtdb.europe-west1.firebasedatabase.app"
        )

        loadCarParkName(carparkId)
        loadSlotsAndCalculateAvailability(carparkId)

        buttonToggleSlots.setOnClickListener {
            showSlotsPopup()
        }
    }

    private fun loadCarParkName(carparkId: String) {
        val nameRef = database.getReference("locations")
            .child(carparkId)
            .child("name")

        nameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java) ?: carparkId
                textViewLocationName.text = name
            }

            override fun onCancelled(error: DatabaseError) {
                textViewLocationName.text = carparkId
            }
        })
    }

    private fun loadSlotsAndCalculateAvailability(carparkId: String) {
        val slotsRef = database.getReference("spaces").child(carparkId)

        slotsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                slotList.clear()

                var totalSpaces = 0
                var availableSpaces = 0

                for (space in snapshot.children) {
                    val slotName = space.key ?: continue
                    val isAvailable = space.getValue(Boolean::class.java) ?: false

                    totalSpaces++

                    if (isAvailable) {
                        availableSpaces++
                    }

                    val slot = ParkingSlot(
                        name = slotName,
                        available = isAvailable
                    )

                    slotList.add(slot)
                }

                updateSummary(availableSpaces, totalSpaces)
            }

            override fun onCancelled(error: DatabaseError) {
                textViewAvailable.text = "Failed to load spaces"
                textViewFullness.text = "0% Full"
                progressBarCapacity.progress = 0
            }
        })
    }

    private fun updateSummary(availableSpaces: Int, totalSpaces: Int) {
        textViewAvailable.text = "Available: $availableSpaces / $totalSpaces"

        if (totalSpaces > 0) {
            val occupiedSpaces = totalSpaces - availableSpaces
            val percentFull = (occupiedSpaces * 100) / totalSpaces

            textViewFullness.text = "$percentFull% Full"
            progressBarCapacity.progress = percentFull
        } else {
            textViewFullness.text = "0% Full"
            progressBarCapacity.progress = 0
        }
    }

    private fun showSlotsPopup() {
        if (slotList.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Parking Slots")
                .setMessage("No parking spaces found.")
                .setPositiveButton("Close", null)
                .show()
            return
        }

        val slotDisplayList = slotList.map { slot ->
            val status = if (slot.available) "Available" else "Occupied"
            "${slot.name} - $status"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Parking Slots")
            .setItems(slotDisplayList, null)
            .setPositiveButton("Close", null)
            .show()
    }
}