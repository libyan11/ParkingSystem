package com.example.parkingapp.model

data class CarPark(
    val id: String = "",
    val name: String = "",
    val available: Int = 0,
    val total: Int = 0,
    val location: String = "",
    val imageUrl: String = ""
)