package com.fh.theposition.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.fh.theposition.data.ui.activity.MainActivity
import com.fh.theposition.R
import com.fh.theposition.util.Constants.ACTION_PAUSE_SERVICE
import com.fh.theposition.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.fh.theposition.util.Constants.ACTION_STOP_SERVICE
import com.fh.theposition.util.Constants.NOTIFICATION_CHANNEL_ID
import com.fh.theposition.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.fh.theposition.util.Constants.NOTIFICATION_ID
import com.fh.theposition.util.Constants.START_MAP_FRAGMENT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng


typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

class TrackingService : LifecycleService() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isServiceStopped = false


    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLines>()
    }

    private fun initializeDefaultValues() {
        isTracking.value = false
        pathPoints.value = mutableListOf()
    }


    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        initializeDefaultValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocation(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    startForegroundServices()
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    stopService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.value = false
        addEmptyPolyline()
    }

    private fun stopService() {
        isServiceStopped = true
        initializeDefaultValues()
        pauseService()
        stopForeground(true)
        stopSelf()


    }

    private val locationCallBack = object : LocationCallback() {
        @SuppressLint("LogNotTimber")
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let {
                    for (location in it) {
                        addPathPoints(location)
                        Log.i(
                            "TAG",
                            "onLocationResult:${location.latitude}, ${location.longitude} "
                        )
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {
        if (isTracking) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000L
                fastestInterval = 2000L
                priority = PRIORITY_HIGH_ACCURACY
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoints(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }

    }

    private fun startForegroundServices() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.icon_clock)
            .setContentTitle("hello")
            .setContentText("00.00.00")
            .setContentIntent(getPendingIntentForMap())
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun getPendingIntentForMap() =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = START_MAP_FRAGMENT

            },
            FLAG_UPDATE_CURRENT
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

}