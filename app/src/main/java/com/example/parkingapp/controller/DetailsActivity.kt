package com.example.parkingapp.controller

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.parkingapp.R
import com.example.parkingapp.model.ParkingSlot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailsActivity : AppCompatActivity() {

    private lateinit var imageViewLocation: ImageView
    private lateinit var textViewLocationName: TextView
    private lateinit var textViewLocationAddress: TextView
    private lateinit var textViewAvailable: TextView
    private lateinit var textViewFullness: TextView
    private lateinit var progressBarCapacity: ProgressBar
    private lateinit var buttonToggleSlots: Button

    private lateinit var database: FirebaseDatabase
    private val slotList = ArrayList<ParkingSlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        imageViewLocation = findViewById(R.id.imageViewLocation)
        textViewLocationName = findViewById(R.id.textViewLocationName)
        textViewLocationAddress = findViewById(R.id.textViewLocationAddress)
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

        loadCarParkDetails(carparkId)
        loadSlotsAndCalculateAvailability(carparkId)

        buttonToggleSlots.setOnClickListener {
            showSlotsPopup()
        }
    }

    private fun loadCarParkDetails(carparkId: String) {
        val locationRef = database.getReference("locations").child(carparkId)

        locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name")
                    .getValue(String::class.java)
                    ?.trim()
                    ?: carparkId

                val location = snapshot.child("location")
                    .getValue(String::class.java)
                    ?.trim()
                    ?: "Location not available"

                val imageUrl = snapshot.child("imageUrl")
                    .getValue(String::class.java)
                    ?.trim()
                    ?: ""

                textViewLocationName.text = name
                textViewLocationAddress.text = location.ifEmpty { "Location not available" }

                if (imageUrl.isNotEmpty()) {
                    Glide.with(this@DetailsActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.icon)
                        .error(R.drawable.icon)
                        .centerCrop()
                        .into(imageViewLocation)
                } else {
                    imageViewLocation.setImageResource(R.drawable.icon)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                textViewLocationName.text = carparkId
                textViewLocationAddress.text = "Location not available"
                imageViewLocation.setImageResource(R.drawable.icon)
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
                    val value = space.value

                    val isAvailable = when (value) {
                        is Boolean -> value
                        is Long -> value == 1L
                        is Int -> value == 1
                        is Double -> value == 1.0
                        is String -> value == "1" || value.equals("true", ignoreCase = true)
                        else -> false
                    }

                    totalSpaces++

                    if (isAvailable) {
                        availableSpaces++
                    }

                    slotList.add(
                        ParkingSlot(
                            name = slotName,
                            available = isAvailable
                        )
                    )
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