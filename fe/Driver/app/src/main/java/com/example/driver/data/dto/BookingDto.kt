package com.example.driver.data.dto

import com.example.driver.utils.TypeCar

data class BookingDto (
    val destination: LatLong,
    val source: LatLong,
    val phoneNumber: String,
    val vehicleType: TypeCar
)