package com.example.user.presentation.searching

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.user.BuildConfig
import com.example.user.R
import com.example.user.data.api.AuthenticationApi
import com.example.user.data.model.googlemap.ResultPlaceClient
import com.example.user.databinding.ActivitySearchingBinding
import com.example.user.presentation.BaseActivity
import com.example.user.utils.Constant.decodePoly
import com.example.user.utils.Status
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.*
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SearchingActivity : BaseActivity() {

    @Inject
    lateinit var searchingViewModel: SearchingViewModel
    @Inject
    lateinit var authenticationApi: AuthenticationApi

    private lateinit var placesClient: PlacesClient
    private lateinit var loadPlacesFromGoogleMap: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivitySearchingBinding
    private var isOrigin: Boolean? = null
    gprivate lateinit var map: GoogleMap
    private val intentGoogleMap by lazy {
        Autocomplete
            .IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                listOf(Place.Field.ID, Place.Field.NAME)
            )
            .build(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_searching)
        binding.lifecycleOwner = this
        binding.viewModel = searchingViewModel



//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.e("TAG", "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//            val msg = task.result
//            Log.e("TAG", msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })

        if(!Places.isInitialized())
            Places.initialize(this, BuildConfig.GOOGLE_MAP_API)
        placesClient = Places.createClient(this)

        setupLoadPlaceFromGoogleMap()
        setupHandleEventListener()
        registerObserve()
    }

    private fun setupHandleEventListener() {
        binding.etDestination.setOnClickListener {
            isOrigin = false
            loadPlacesFromGoogleMap.launch(intentGoogleMap)
        }
        binding.etOrigin.setOnClickListener {
            isOrigin = true
            loadPlacesFromGoogleMap.launch(intentGoogleMap)
        }
        (supportFragmentManager.findFragmentById(R.id.map_view_in_searching_activity)
                as SupportMapFragment).getMapAsync {
                    this.map = it
        }
    }

    private fun registerObserve(){
        searchingViewModel.resultPlaceClient.observe(this){
            when(it.status){
                Status.LOADING -> Toast.makeText(this,"Loading...",Toast.LENGTH_LONG).show()
                Status.ERROR -> Toast.makeText(this,"Cant load data",Toast.LENGTH_LONG).show()
                Status.SUCCESS -> {
                    if(isOrigin == true) {
                        searchingViewModel.setOrigin(it.data)
                        it.data?.let { rps ->
                            binding.etOrigin.text = rps.formatted_address
                        }
                    }
                    else if (isOrigin == false) {
                        searchingViewModel.setDestination(it.data)
                        it.data?.let { rps ->
                            binding.etDestination.text = rps.formatted_address
                        }
                    }
                    isOrigin = null
                }
            }
        }
        searchingViewModel.routes.observe(this){ routes ->
            if(routes?.isNotEmpty() == true){
                val points = mutableListOf<LatLng>()
                routes.forEach { route ->
                    route.legs.forEach { leg ->
                        leg.steps.forEach { step ->
                            points.addAll(decodePoly(step.polyline.points))
                        }
                    }
                }
                PolylineOptions().apply {
                    this.addAll(points)
                    this.width(10f)
                    this.color(Color.RED)
                    this.geodesic(true)
                }.let {
                    map.addPolyline(it)
                }
                val bounds = LatLngBounds.Builder()

                addMarker(bounds,searchingViewModel.origin!!,"Marker 1")
                addMarker(bounds,searchingViewModel.destination!!,"Marker 2")

                val point = Point()
                windowManager.defaultDisplay.getSize(point)
                map.animateCamera(
                    CameraUpdateFactory
                        .newLatLngBounds(
                            bounds.build(),
                            point.x,
                            550,
                            230
                        )
                )
            }else{
                Log.e("5","hello")
            }
        }
    }

    private fun setupLoadPlaceFromGoogleMap(){
        loadPlacesFromGoogleMap =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
                when(result.resultCode) {
                    Activity.RESULT_OK -> {
                        result.data?.let {
                            val place = Autocomplete.getPlaceFromIntent(result.data!!)
                            place.id?.let { it1 -> searchingViewModel.getAddress(it1) }
                        }
                    }
                    AutocompleteActivity.RESULT_ERROR -> {
                        result.data?.let {
                            val status = Autocomplete.getStatusFromIntent(result.data!!)
                            Log.i("TAG", status.statusMessage ?: "")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // The user canceled the operation.
                    }
                }
            }
    }

    private fun addMarker(bounds: LatLngBounds.Builder, place: ResultPlaceClient, marker: String){
        val latLong = LatLng(place.geometry.location.lat, place.geometry.location.lng)
        map.addMarker(
            MarkerOptions().position(latLong)
                .title(marker)
        )
        bounds.include(latLong)
    }
}