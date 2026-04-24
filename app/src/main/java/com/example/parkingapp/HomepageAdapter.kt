package com.example.parkingapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class HomepageAdapter(
    private val context: Context,
    private val names: List<String>,
    private val spaces: List<Int>
) : BaseAdapter() {

    override fun getCount(): Int {
        return names.size
    }

    override fun getItem(position: Int): Any {
        return names[position]
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

        val name = names[position]
        val available = spaces[position]

        textViewParkingName.text = name
        textViewSpaces.text = "Available Spaces: $available"

        if (available > 0) {
            textViewStatus.text = "Status: Available"
            textViewStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            textViewStatus.text = "Status: Full"
            textViewStatus.setTextColor(Color.RED)
        }

        imageParking.setImageResource(R.drawable.icon)

        return view
    }
}