package com.fh.theposition.services

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.fh.theposition.util.Constants.ACTION_START_FUSED_SERVICE
import com.fh.theposition.util.Constants.ACTION_STOP_FUSED_SERVICE
import com.fh.theposition.util.PermissionUtil
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.flow.MutableStateFlow

class FusedLocationService : LifecycleService() {


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        val latitudeFlow = MutableLiveData<Location>()
    }


    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient= FusedLocationProviderClient(applicationContext)

        locationRequest = LocationRequest.create().apply {
            interval = 5000L
            fastestInterval = 2000L
            priority = PRIORITY_HIGH_ACCURACY
        }


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                latitudeFlow.value = locationResult.lastLocation

            }
        }


    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (intent.action) {
                ACTION_START_FUSED_SERVICE -> {

                    requestLastLocation()
                }
                ACTION_STOP_FUSED_SERVICE -> {
                    stopRequestLocation()
                }
                else -> {
                    /*NO_OP*/
                }
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    private fun requestLastLocation() {

        if (PermissionUtil.checkPermission(applicationContext)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                   latitudeFlow.value=it
                }
                requestCurrentLocation()
            }

        }

    }

    private fun stopRequestLocation() {
        stopSelf()
    }

    private fun requestCurrentLocation() {
        if (PermissionUtil.checkPermission(applicationContext)) {
            fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(applicationContext)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}