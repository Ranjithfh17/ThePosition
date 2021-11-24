package com.fh.theposition.data.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentClockBinding
import com.fh.theposition.util.AsyncImageLoader.loadImage
import com.fh.theposition.util.ClockSkins.clockNeedleSkins
import com.fh.theposition.util.DigitalTimeConverter.getTime
import com.fh.theposition.util.TimeConverter.getHoursInDegree
import com.fh.theposition.util.TimeConverter.getMinutesInDegree
import com.fh.theposition.util.TimeConverter.getSecondsInDegree
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class Clock : Fragment(R.layout.fragment_clock) {

    private lateinit var binding: FragmentClockBinding
    private lateinit var timeZone:String
    private var movementType = "oscillate"
    private lateinit var handler: Handler
    @Inject
    lateinit var zonedDateTime: ZonedDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClockBinding.bind(view)

        handler = Handler(Looper.getMainLooper())
        binding.mainLayout.setProxyView(view)

        timeZone=zonedDateTime.zone.toString()

        setSkins()


    }

    private fun setSkins() {
        setNeedle(1)
    }

    private fun setNeedle(value: Int) {
        loadImage(clockNeedleSkins[value][0], binding.hour, requireContext(), 0)
        loadImage(clockNeedleSkins[value][1], binding.minute, requireContext(), 100)
        loadImage(clockNeedleSkins[value][2], binding.seconds, requireContext(), 200)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTime(): ZonedDateTime {
        val zoneId = ZoneId.of(timeZone)
        return Instant.now().atZone(zoneId)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private val clock = object : Runnable {
        override fun run() {
            binding.hour.rotation = getHoursInDegree(getCurrentTime())
            binding.minute.rotation = getMinutesInDegree(getCurrentTime())

            when (movementType) {
                "oscillate" -> {
                    binding.apply {
                        seconds.setPhysical(-1F, -5F, 5000F)
                        seconds.rotationUpdate(getSecondsInDegree(getCurrentTime()), true)
                        sweepSeconds.setPhysical(-1F, -5F, 5000F)
                        sweepSeconds.rotationUpdate(
                            getSecondsInDegree(getCurrentTime()) - 90F,
                            true
                        )
                    }
                }
            }

            handler.postDelayed(this, 1000)

        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTimeData() {
        try {
            val zoneId = ZoneId.of(timeZone)
            val zonedTimeData: ZonedDateTime = Instant.now().atZone(zoneId)
            binding.digitalTimeMain.text = getTime(zonedTimeData)

        } catch (exception: Exception) {

        }

    }


    private val updateTime = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            updateTimeData()
            handler.postDelayed(this, 1000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        handler.post(clock)
        handler.post(updateTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clock)
        handler.removeCallbacks(updateTime)
    }




}