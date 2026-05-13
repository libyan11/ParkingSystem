package com.example.parkingapp.controller.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
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
        val textViewSpaces = view.findViewById<TextView>(R.id.textViewSpaces)
        val textViewStatus = view.findViewById<TextView>(R.id.textViewStatus)

        val carpark = carparks[position]

        textViewParkingName.text = carpark.name
        textViewSpaces.text = "Available Spaces: ${carpark.available}"

        if (carpark.available > 0) {
            textViewStatus.text = "Status: Available"
            textViewStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            textViewStatus.text = "Status: Full"
            textViewStatus.setTextColor(Color.RED)
        }

        imageParking.setImageResource(R.drawable.dmu_icon)

        return view
    }
}