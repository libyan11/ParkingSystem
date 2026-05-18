package com.example.parkingapp.controller.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.parkingapp.R
import com.example.parkingapp.model.CarPark

class HomepageAdapter(
    private val context: Context,
    private val carparks: List<CarPark>
) : BaseAdapter() {

    override fun getCount(): Int {
        return carparks.size
    }

    override fun getItem(position: Int): Any {
        return carparks[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_parking, parent, false)

        val imageParking = view.findViewById<ImageView>(R.id.imageParking)
        val textViewParkingName = view.findViewById<TextView>(R.id.textViewParkingName)
        val textViewLocation = view.findViewById<TextView>(R.id.textViewLocation)
        val textViewSpaces = view.findViewById<TextView>(R.id.textViewSpaces)
        val textViewStatus = view.findViewById<TextView>(R.id.textViewStatus)

        val carpark = carparks[position]

        textViewParkingName.text = carpark.name
        textViewLocation.text = carpark.location
        textViewSpaces.text = "Available Spaces: ${carpark.available}"

        if (carpark.available > 0) {
            textViewStatus.text = "Status: Available"
            textViewStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            textViewStatus.text = "Status: Full"
            textViewStatus.setTextColor(Color.RED)
        }

        val imageUrl = carpark.imageUrl.trim()

        if (imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.icon)
                .error(R.drawable.icon)
                .centerCrop()
                .into(imageParking)
        } else {
            imageParking.setImageResource(R.drawable.icon)
        }

        return view
    }
}