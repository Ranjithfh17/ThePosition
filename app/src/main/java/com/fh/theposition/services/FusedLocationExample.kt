package com.fh.theposition.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.HandlerThread
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fh.theposition.util.PermissionUtil
import com.google.android.gms.location.*

/**
 * Service class to use [FusedLocationProviderClient] as location
 * provider and broadcast the location throughout the app.
 */
class FusedLocationExample : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var handlerThread: HandlerThread

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread = HandlerThread("location_thread")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationRequest = LocationRequest.create()
                .setInterval(1000L)
                .setFastestInterval(1000L)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        requestLastLocation()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                    broadcastLocation(result.lastLocation)

            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                p0.isLocationAvailable
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLastLocation()
        if (!handlerThread.isAlive) {
            handlerThread.start()
        }
        return START_REDELIVER_INTENT
    }

    private fun requestLastLocation() {
        if (PermissionUtil.checkPermission(applicationContext)) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    broadcastLocation(it.result)
                }

                requestCurrentLocation()
            }
        }
    }

    private fun requestCurrentLocation() {
        if (PermissionUtil.checkPermission(applicationContext)) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, handlerThread.looper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        handlerThread.quitSafely()
    }

    private fun broadcastLocation(location: Location) {
        Intent().also { intent ->
            intent.action = "location"
            intent.putExtra("location", location)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        }
    }
}
