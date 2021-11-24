package com.fh.theposition.data.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fh.theposition.R
import com.fh.theposition.callbacks.OnMapClickListener
import com.fh.theposition.databinding.FragmentMapBinding
import com.fh.theposition.databinding.LayoutMapBottomSheetBinding
import com.fh.theposition.math.MathExtensions.round
import com.fh.theposition.services.PolyLine
import com.fh.theposition.services.TrackingService
import com.fh.theposition.util.BitmapHelper.toBitMap
import com.fh.theposition.util.HtmlFormatter.fromHtml
import com.fh.theposition.util.LocationUtil
import com.fh.theposition.util.hasInternetConnection
import com.fh.theposition.viewmodels.MainPrefViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collect
import java.util.*

class Map : Fragment(R.layout.fragment_map) {

    private val TAG = "Map"
    private lateinit var binding: FragmentMapBinding
    private lateinit var bottomSheetBinding: LayoutMapBottomSheetBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel by activityViewModels<MainPrefViewModel>()

    private var googleMap: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<PolyLine>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var scrollView: NestedScrollView
    private lateinit var locationData: TextView
    private lateinit var coordinateData: TextView
    private lateinit var addressData: TextView
    private lateinit var adView: AdView
    private lateinit var noConnectionLayout: ConstraintLayout
    private lateinit var infoLayout: ConstraintLayout
    private lateinit var refreshButton:ImageView
    private var marker: Bitmap? = null


    @SuppressLint("VisibleForTests")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapBinding.bind(view)
        binding.mapView.onCreate(savedInstanceState)
        bottomSheetBinding = LayoutMapBottomSheetBinding.bind(binding.root)

        fusedLocationProviderClient = FusedLocationProviderClient(requireContext())
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.map_bottom_container))
        scrollView = view.findViewById(R.id.scroll_view)
        locationData = view.findViewById(R.id.location_data)
        coordinateData = view.findViewById(R.id.coordinate_data)
        addressData = view.findViewById(R.id.address_data)
        adView = view.findViewById(R.id.adView)
        noConnectionLayout = view.findViewById(R.id.no_connection_layout)
        infoLayout = view.findViewById(R.id.info_layout)
        refreshButton = view.findViewById(R.id.refresh_button)

        if (!LocationUtil.getLocationStatus(requireContext())) {
            LocationUtil.displayLocationSettingsRequest(requireContext(), requireActivity())
        }


        binding.mapView.getMapAsync {
            googleMap = it
            addAllPolyLines()
            googleMap?.setOnMarkerDragListener(dragListener)
        }

        binding.mapView.getMapAsync(onMapReadyCallback)
        init()

        binding.mapType.setOnClickListener {
            showMapTypeDialog()
        }






        getMapType()

        setUpObservers()

        setBottomSheetBehaviour()

        checkConnection()

        MobileAds.initialize(requireContext()) {}
        loadAdds()
        refreshLayouts()




        //        binding.track.setOnClickListener {
//            startTracking()
//        }
//
//        binding.stop.setOnClickListener {
//            sendCommandToService(ACTION_STOP_SERVICE)
//            pathPoints.clear()
//            googleMap?.clear()
//        }


    }



    private fun refreshLayouts(){
        refreshButton.setOnClickListener {
            ObjectAnimator.ofFloat(refreshButton, "rotation", 360f, 0f).setDuration(500).start()
            checkConnection()
        }
    }



    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val position =
                LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18F))


            setMarker(locationResult.lastLocation)

            locationData.text = fromHtml(
                "<b>${getString(R.string.accuracy)} </b> ${
                    round(
                        locationResult.lastLocation.accuracy.toDouble(),
                        2
                    )
                }<b> m <br><br>" +
                        "<b> ${getString(R.string.altitude)} </b> ${
                            round(
                                locationResult.lastLocation.altitude,
                                2
                            )
                        }<b> m"
            )

            coordinateData.text = fromHtml(
                "<b> ${getString(R.string.latitude)}</b> ${locationResult.lastLocation.latitude}<br><br>" +
                        "<b> ${getString(R.string.longitude)}</b> ${locationResult.lastLocation.longitude}"
            )

            getAddress(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)


        }
    }

    private fun setMarker(lastLocation: Location) {
        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        marker = R.drawable.icon_location.toBitMap(requireContext(), 200)
        googleMap?.clear()
        googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(marker))
                .draggable(true)
        )

    }

    private val dragListener = object : GoogleMap.OnMarkerDragListener {
        override fun onMarkerDragStart(p0: Marker) {
        }

        override fun onMarkerDrag(p0: Marker) {
        }

        override fun onMarkerDragEnd(marker: Marker) {
            val latLng = marker.position
            marker.snippet = latLng.toString()
        }
    }

    private fun getAddress(latitude: Double, longitude: Double) {

        val address = try {
            val addressList: List<Address>
            val geoCoder = Geocoder(requireContext(), Locale.getDefault())
            addressList = geoCoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                addressList[0].getAddressLine(0)

            } else {
                "No address found at this place"
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        try {
            if(hasInternetConnection(requireContext())){
                addressData.text = address.toString()
            }else{
                addressData.text = getString(R.string.no_internet_connection_available)

            }
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }


    }


    @SuppressLint("MissingPermission")
    private val onMapReadyCallback = OnMapReadyCallback { map ->
        googleMap = map


        googleMap?.isMyLocationEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.uiSettings?.isCompassEnabled = false

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val pos = LatLng(it.latitude, it.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18F))
            }

        }

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
            Configuration.UI_MODE_NIGHT_YES ->{
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.night_styled_map))
            }
        }






        getMapType()

    }


    @SuppressLint("MissingPermission")
    private fun init() {
        val locationRequest = LocationRequest.create().apply {
            smallestDisplacement = 10f
            interval = 5000L
            fastestInterval = 3000L
            priority = PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )


    }


    private fun setUpObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
//            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyLines()
            animateCamera()
        }
    }

//    private fun startTracking() {
//        if (isTracking) {
//            sendCommandToService(ACTION_PAUSE_SERVICE)
//            binding.stop.isVisible = true
//        } else {
//            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
//        }
//    }
//
//    private fun updateTracking(isTracking: Boolean) {
//        this.isTracking = isTracking
//        binding.stop.isVisible = isTracking
//    }

    private fun addAllPolyLines() {
        for (polyLines in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.black))
                .width(8f)
                .addAll(polyLines)
            googleMap?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLines() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.purple_200))
                .width(10f)
                .add(preLatLng)
                .add(lastLatLng)

            googleMap?.addPolyline(polylineOptions)

        }
    }

    private fun animateCamera() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    18F
                )
            )
        }
    }


    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }

    private fun showMapTypeDialog() {
        findNavController().navigate(R.id.mapTypeDialog)
    }

    private fun getMapType() {
        lifecycleScope.launchWhenStarted {
            viewModel.getMapType("type").collect { type ->
                Log.i("TAG", "getMapType:$type ")
                when (type) {
                    "Normal" -> {
                        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }

                    "Hybrid" -> {
                        googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                    }

                    "Terrain" -> {
                        googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }

                    "Satellite" -> {
                        googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE

                    }
                }

            }
        }
    }

    private fun setBottomSheetBehaviour() {


        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                scrollView.alpha = slideOffset

            }

        })
    }

    private fun checkConnection() {
        if (!hasInternetConnection(requireContext())) {
            noConnectionLayout.visibility = View.VISIBLE
            infoLayout.visibility = View.GONE
        } else {
            noConnectionLayout.visibility = View.GONE
            infoLayout.visibility = View.VISIBLE
        }
    }


    private fun loadAdds() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.i(TAG, "onAdLoaded: ad loaded successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                Log.i(TAG, "onAdFailedToLoad:${error} ")

            }
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)

    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()

    }


}