package com.example.parkingapp.controller

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.parkingapp.R
import com.example.parkingapp.controller.adapters.HomepageAdapter
import com.example.parkingapp.model.CarPark
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var textViewLastUpdated: TextView
    private lateinit var buttonAccount: ImageButton

    private lateinit var database: DatabaseReference

    private val carparkList = ArrayList<CarPark>()
    private val previousAvailability = HashMap<String, Int>()

    private var lastUpdatedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        listView = findViewById(R.id.listViewParking)
        textViewLastUpdated = findViewById(R.id.textViewLastUpdated)
        buttonAccount = findViewById(R.id.buttonAccount)

        buttonAccount.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }

        createNotificationChannel()

        database = FirebaseDatabase.getInstance(
            "https://parking-1034e-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference("locations")

        loadCarParks()
    }

    private fun loadCarParks() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                carparkList.clear()

                for (carparkSnapshot in snapshot.children) {
                    val key = carparkSnapshot.key ?: ""

                    val name = carparkSnapshot.child("name")
                        .getValue(String::class.java) ?: key

                    val available = carparkSnapshot.child("available")
                        .getValue(Number::class.java)
                        ?.toInt() ?: 0

                    val total = carparkSnapshot.child("total")
                        .getValue(Number::class.java)
                        ?.toInt() ?: 0

                    val carpark = CarPark(
                        id = key,
                        name = name,
                        available = available,
                        total = total
                    )

                    carparkList.add(carpark)

                    val previous = previousAvailability[key]

                    if (previous != null && previous == 0 && available > 0) {
                        showNotification(name, available)
                    }

                    previousAvailability[key] = available
                }

                val adapter = HomepageAdapter(
                    this@HomeActivity,
                    carparkList
                )

                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
                    intent.putExtra("carparkId", carparkList[position].id)
                    startActivity(intent)
                }

                lastUpdatedTime = System.currentTimeMillis()
                updateLastUpdatedText()
            }

            override fun onCancelled(error: DatabaseError) {
                textViewLastUpdated.text = "Failed to load parking data"
            }
        })
    }

    private fun showNotification(carparkName: String, available: Int) {
        val intent = Intent(this, HomeActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "parking_channel")
            .setSmallIcon(R.drawable.icon_account)
            .setContentTitle("Parking Available")
            .setContentText("$carparkName now has $available spaces available.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "parking_channel",
                "Parking Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
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