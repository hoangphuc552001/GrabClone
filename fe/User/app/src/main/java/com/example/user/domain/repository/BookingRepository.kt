package com.example.user.domain.repository

import com.example.user.data.dto.BookingDto
import com.example.user.data.dto.RegisterFCMBody
import com.example.user.data.model.booking.ResponseBooking
import retrofit2.Response

interface BookingRepository {
    suspend fun bookingDriver(bookingDto: BookingDto): Response<ResponseBooking>
    suspend fun postRegisterFcmToken(registerFCMBody: RegisterFCMBody): Response<Int>
}