package com.example.parkingapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var textViewLastUpdated: TextView

    private val carparkNames = ArrayList<String>()
    private val carparkSpaces = ArrayList<Int>()
    private val carparkKeys = ArrayList<String>()

    private val previousAvailability = HashMap<String, Int>()

    private var lastUpdatedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        listView = findViewById(R.id.listViewParking)
        textViewLastUpdated = findViewById(R.id.textViewLastUpdated)

        createNotificationChannel()

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
                    val key = carpark.key ?: ""

                    if (name != null && available != null) {
                        carparkNames.add(name)
                        carparkSpaces.add(available)
                        carparkKeys.add(key)

                        val previous = previousAvailability[key]

                        if (previous != null && previous == 0 && available > 0) {
                            showNotification(name, available)
                        }

                        previousAvailability[key] = available
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

    private fun showNotification(carparkName: String, available: Int) {
        val intent = Intent(this, HomeActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "parking_channel")
            .setSmallIcon(R.drawable.icon)
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