package com.example.parkingapp.controller

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.R
import com.example.parkingapp.model.CarPark
import com.example.parkingapp.model.ParkingSlot
import com.google.firebase.database.*

class DetailsActivity : AppCompatActivity() {

    private lateinit var textViewLocationName: TextView
    private lateinit var textViewAvailable: TextView
    private lateinit var textViewFullness: TextView
    private lateinit var progressBarCapacity: ProgressBar
    private lateinit var buttonToggleSlots: Button
    private lateinit var listViewSpaces: ListView

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
        listViewSpaces = findViewById(R.id.listViewSpaces)

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

        loadSummary(carparkId)
        loadSlots(carparkId)

        buttonToggleSlots.setOnClickListener {
            if (listViewSpaces.visibility == View.GONE) {
                listViewSpaces.visibility = View.VISIBLE
                buttonToggleSlots.text = "Hide Parking Slots"
            } else {
                listViewSpaces.visibility = View.GONE
                buttonToggleSlots.text = "Show Parking Slots"
            }
        }
    }

    private fun loadSummary(carparkId: String) {
        val summaryRef = database.getReference("locations").child(carparkId)

        summaryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carpark = CarPark(
                    id = carparkId,
                    name = snapshot.child("name").getValue(String::class.java) ?: "Car Park Name",
                    available = snapshot.child("available").getValue(Int::class.java) ?: 0,
                    total = snapshot.child("total").getValue(Int::class.java) ?: 0
                )

                textViewLocationName.text = carpark.name
                textViewAvailable.text = "Available: ${carpark.available} / ${carpark.total}"

                if (carpark.total > 0) {
                    val occupied = carpark.total - carpark.available
                    val percentFull = (occupied * 100) / carpark.total
                    textViewFullness.text = "$percentFull% Full"
                    progressBarCapacity.progress = percentFull
                } else {
                    textViewFullness.text = "0% Full"
                    progressBarCapacity.progress = 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadSlots(carparkId: String) {
        val slotsRef = database.getReference("spaces").child(carparkId)

        slotsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                slotList.clear()

                for (space in snapshot.children) {
                    val slotName = space.key
                    val slotValue = space.getValue(Boolean::class.java)

                    if (slotName != null && slotValue != null) {
                        val slot = ParkingSlot(
                            name = slotName,
                            available = slotValue
                        )

                        slotList.add(slot)
                    }
                }

                val slotDisplayList = slotList.map { slot ->
                    val status = if (slot.available) "Available" else "Occupied"
                    "${slot.name} - $status"
                }

                val adapter = ArrayAdapter(
                    this@DetailsActivity,
                    android.R.layout.simple_list_item_1,
                    slotDisplayList
                )

                listViewSpaces.adapter = adapter
                setListViewHeightBasedOnChildren(listViewSpaces)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val adapter = listView.adapter ?: return
        var totalHeight = 0

        for (i in 0 until adapter.count) {
            val item = adapter.getView(i, null, listView)
            item.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += item.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (adapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }
}