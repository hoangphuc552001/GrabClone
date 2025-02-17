package com.example.driver.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.example.driver.data.model.authentication.BodyAccessToken
import com.example.driver.data.model.authentication.BodyRefreshToken
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.util.*


object Constant {
    const val HAVE_NEW_BOOKING: String = "HAVE_NEW_BOOKING"
    const val HAVE_NEW_BOOKING_EXTRA: String = "HAVE_NEW_BOOKING_EXTRA"
    const val REFRESH_TOKEN_EXPIRED_WHEN_SEND_LOCATION_EXTRA_USERNAME: String = "REFRESH_TOKEN_EXPIRED_EXTRA_USERNAME"
    const val REFRESH_TOKEN_EXPIRED_WHEN_SEND_LOCATION: String = "REFRESH_TOKEN_EXPIRED"
    const val REQUEST_CURRENT_LOCATION = 1


    fun convertLatLongToAddress(context: Context, latLng: LatLng): String {
        val addressList: List<Address>? = Geocoder(
            context,
            Locale.getDefault()
        ).getFromLocation(latLng.latitude, latLng.longitude, 1)

        val sb = StringBuilder()
        if (addressList != null && addressList.isNotEmpty()) {
            val address: Address = addressList[0]
            for (i in 0..address.maxAddressLineIndex) {
                sb.append(address.getAddressLine(i)).append(",")
            }
            sb.deleteCharAt(sb.length - 1)
        }
        return sb.toString()
    }


    fun decodePoly(encoded: String): List<LatLng> {
        Log.i("Location", "String received: $encoded")
        val poly = ArrayList<LatLng>()
        var index = 0
        val len: Int = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = (encoded[index++] - 63).code
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = (encoded[index++] - 63).code
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)

            poly.add(p)
        }

        for (i in 0 until poly.size) {
            Log.i(
                "Location",
                "Point sent: Latitude: " + poly[i].latitude.toString() + " Longitude: " + poly[i].longitude
            )
        }
        return poly
    }

    fun getPayloadDataFromJWTRefreshToken(token: String): BodyRefreshToken =
        Gson().fromJson(
            String(Base64.getDecoder().decode(token.split('.')[1])),
            BodyRefreshToken::class.java
        )
    fun getPayloadDataFromJWTAccessToken(token: String): BodyAccessToken =
        Gson().fromJson(
            String(Base64.getDecoder().decode(token.split('.')[1])),
            BodyAccessToken::class.java
        )

    fun getErrorBody(){

//            val type = object : TypeToken<ResponseValidateRegister>() {}.type
//            var errorResponse: ResponseValidateRegister? = Gson().fromJson(b.errorBody()!!.charStream(), type)
//            Log.e("-----", errorResponse.toString())
    }

    fun convertTimeLongToDateTime(time: Long): Date = Date(time * 1000)
    fun getCurrentDate() = Date()

    fun checkPhone(str: String): Boolean =
        str.matches("^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$".toRegex())
}