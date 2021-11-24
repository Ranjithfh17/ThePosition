package com.fh.theposition.data.ui.fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentHomeBinding
import com.fh.theposition.databinding.LayoutDialogBinding
import com.fh.theposition.math.MathExtensions.round
import com.fh.theposition.services.FusedLocationService
import com.fh.theposition.util.DigitalTimeConverter.getTimeWithSeconds
import com.fh.theposition.util.Direction.getDirectionCodeFromAzimuth
import com.fh.theposition.util.HtmlFormatter.fromHtml
import com.fh.theposition.util.MoonAngle.getMoonPhase
import com.fh.theposition.util.MoonTimeFormatter.formatMoonDate
import com.fh.theposition.util.toOrdinal
import com.fh.theposition.viewmodels.MainPrefViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var timeZone: String
    private val viewModel by activityViewModels<MainPrefViewModel>()

    @Inject
    lateinit var zonedDateTime: ZonedDateTime



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)


        lifecycleScope.launchWhenStarted {
            viewModel.readName("name").collect { name ->
                binding.nameText.text = name
            }

        }

        FusedLocationService.latitudeFlow.observe(viewLifecycleOwner){
            calculateData(it.latitude,it.longitude)
        }


        timeZone = zonedDateTime.zone.toString()


        setWelcomeMessage()

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.settings2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateData(latitude: Double, longitude: Double) {

        CoroutineScope(Dispatchers.Default).launch {

            val sunTimeData: Spanned
            val moonTimeData: Spanned
            val sunPositionData: Spanned
            val moonPositionData: Spanned
            val twilightData: Spanned
            val moonIlluminationData: Spanned

            withContext(Dispatchers.Main) {
                val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")

                val sunTime =
                    SunTimes.compute().timezone(timeZone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
                sunTimeData = fromHtml(
                    "<b> ${getString(R.string.sun_rise)}<b> ${pattern.format(sunTime.rise)}<br><br>" +
                            "<b>${getString(R.string.sunset)}<b> ${pattern.format(sunTime.set)}<br><br>" +
                            "<b> ${getString(R.string.noon)}<b> ${pattern.format(sunTime.noon)}<br><br>" +
                            "<b> ${getString(R.string.nadir)} <b>  ${pattern.format(sunTime.nadir)}<br>"
                )

                val moonTimes =
                    MoonTimes.compute().timezone(timeZone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
                moonTimeData = fromHtml(
                    "<b> ${getString(R.string.moon_rise)} <b> ${pattern.format(moonTimes.rise)}<br><br>" +
                            "<b> ${getString(R.string.moon_set)} <b> ${pattern.format(moonTimes.set)}<br>"
                )

                val sunPosition =
                    SunPosition.compute().timezone(timeZone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
                sunPositionData = fromHtml(
                    "<b> ${getString(R.string.azimuth)} </b> ${round(sunPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), sunPosition.azimuth)} <br><br>" +
                            "<b> ${getString(R.string.altitude)} </b> ${round(sunPosition.altitude, 2)} <br><br>" +
                            "<b> ${getString(R.string.distance)}</b> ${String.format("%.3E", sunPosition.distance)} ${getString(R.string.kilometer)}<br>"
                )


                val moonPosition =
                    MoonPosition.compute().timezone(timeZone).on(Instant.now()).latitude(latitude)
                        .longitude(longitude).execute()
                moonPositionData = fromHtml(
                    "<b> ${getString(R.string.azimuth)} </b> ${round(moonPosition.azimuth, 2)}° ${
                        getDirectionCodeFromAzimuth(
                            requireContext(), moonPosition.azimuth
                        )
                    }<br><br>" +
                            "<b>${getString(R.string.altitude)} </b> ${
                                round(
                                    moonPosition.altitude,
                                    2
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.distance)}</b>${
                                String.format(
                                    "%.3E",
                                    moonPosition.distance
                                )
                            } ${getString(R.string.kilometer)}<br>"
                )

                val twilight =
                    SunTimes.compute().timezone(timeZone).on(Instant.now()).latitude(latitude)
                        .longitude(longitude)
                twilightData = fromHtml(
                    "<b>${getString(R.string.astronomicaldawn)}<b> ${
                        pattern.format(
                            twilight.twilight(
                                SunTimes.Twilight.ASTRONOMICAL
                            ).execute().rise
                        )
                    }<br><br>" +
                            "<b> ${getString(R.string.nautical_dawn)}<b> ${
                                pattern.format(
                                    twilight.twilight(
                                        SunTimes.Twilight.NAUTICAL
                                    ).execute().rise
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.civil_dawn)}<b> ${
                                pattern.format(
                                    twilight.twilight(
                                        SunTimes.Twilight.CIVIL
                                    ).execute().rise
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.civil_dusk)}<b> ${
                                pattern.format(
                                    twilight.twilight(
                                        SunTimes.Twilight.CIVIL
                                    ).execute().set
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.nautical_dusk)}<b> ${
                                pattern.format(
                                    twilight.twilight(
                                        SunTimes.Twilight.NAUTICAL
                                    ).execute().set
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.astronomical_dusk)}<b> ${
                                pattern.format(
                                    twilight.twilight(
                                        SunTimes.Twilight.ASTRONOMICAL
                                    ).execute().set
                                )
                            }<br>"
                )


                val moonIllumination =
                    MoonIllumination.compute().timezone(timeZone).on(Instant.now()).execute()
                moonIlluminationData = fromHtml(
                    "<b> ${getString(R.string.fraction)}<b> ${
                        round(
                            moonIllumination.fraction,
                            2
                        )
                    }<br><br>" +
                            "<b> ${getString(R.string.angle)}<b> ${
                                round(
                                    moonIllumination.angle,
                                    2
                                )
                            }°<br><br>" +
                            "<b> ${getString(R.string.angle_state)}<b> ${
                                if (moonIllumination.angle < 0) getString(
                                    R.string.waxing
                                ) else getString(R.string.waning)
                            }<br><br>" +
                            "<b> ${getString(R.string.phase)}<b> ${
                                getMoonPhase(
                                    requireContext(),
                                    moonIllumination.phase
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.phase_angle)} ${
                                round(
                                    moonIllumination.phase,
                                    2
                                )
                            }°<br>"
                )

                val moonDates: Spanned = fromHtml(
                    "<b> ${getString(R.string.full_moon)}<b> ${
                        formatMoonDate(
                            MoonPhase.compute().timezone(timeZone).on(
                                Instant.now()
                            ).phase(MoonPhase.Phase.FULL_MOON).execute().time
                        )
                    }<br><br>" +
                            "<b> ${getString(R.string.new_moon)} <b> ${
                                formatMoonDate(
                                    MoonPhase.compute().timezone(timeZone).on(
                                        Instant.now()
                                    ).phase(MoonPhase.Phase.NEW_MOON).execute().time
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.first_quarter)} <b> ${
                                formatMoonDate(
                                    MoonPhase.compute().timezone(timeZone).on(
                                        Instant.now()
                                    ).phase(MoonPhase.Phase.FIRST_QUARTER).execute().time
                                )
                            }<br><br>" +
                            "<b> ${getString(R.string.last_quarter)} <b> ${
                                formatMoonDate(
                                    MoonPhase.compute().timezone(timeZone).on(
                                        Instant.now()
                                    ).phase(MoonPhase.Phase.LAST_QUARTER).execute().time
                                )
                            }<br>"
                )





                binding.sunTimesData.text = sunTimeData
                binding.moonTimesData.text = moonTimeData
                binding.sunPositionData.text = sunPositionData
                binding.moonPositionData.text = moonPositionData
                binding.twilightData.text = twilightData
                binding.moonIluminationData.text = moonIlluminationData
                binding.moonDatesData.text = moonDates
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateTimeData() {


        lifecycleScope.launchWhenStarted {


            val zoneId = ZoneId.of(timeZone)
            val zonedDateTime: ZonedDateTime = Instant.now().atZone(zoneId)


            val localTimeData: Spanned = fromHtml(
                "<b> ${getString(R.string.time_zone)}</b> ${zonedDateTime.zone}<br><br> " +
                        "<b> ${getString(R.string.time_24hr)} </b> ${
                            zonedDateTime.format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")
                            )
                        }<br><br>" +
                        "<b>${getString(R.string.time_12hr)}</b> ${
                            zonedDateTime.format(
                                DateTimeFormatter.ofPattern("hh:mm:ss a")
                            )
                        }<br><br> " +
                        "<b>${getString(R.string.date)} ${
                            zonedDateTime.format(
                                DateTimeFormatter.ofPattern(
                                    "dd MMMM,yyyy"
                                )
                            )
                        } <br><br>" +
                        "<b> ${getString(R.string.day)} </b> ${
                            zonedDateTime.format(
                                DateTimeFormatter.ofPattern("EEEE")
                            )
                        } <br><br> " +
                        "<b> ${getString(R.string.day_of_year)} </b> ${LocalDate.now().dayOfYear.toOrdinal()} <br><br>" +
                        "<b> ${getString(R.string.week_of_year)}</b> ${
                            zonedDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toOrdinal()
                        }"
            )

            val utcTimeData: Spanned = fromHtml(
                "<b> ${getString(R.string.local_time_offset)}</b> ${
                    zonedDateTime.format(
                        DateTimeFormatter.ofPattern("XXX").withLocale(Locale.getDefault())
                    ).replace("Z", "+00:00")
                }<br><br>" +
                        "<b>${getString(R.string.time)}</b> ${
                            getTimeWithSeconds(
                                ZonedDateTime.ofInstant(
                                    Instant.now(), ZoneId.of(
                                        ZoneOffset.UTC.toString()
                                    )
                                )
                            )
                        }<br><br>" +
                        "<b>${getString(R.string.date)}</b> ${
                            ZonedDateTime.ofInstant(
                                Instant.now(),
                                ZoneId.of(ZoneOffset.UTC.toString())
                            ).format(DateTimeFormatter.ofPattern("dd MMMM, yyyy"))
                        }"
            )

            binding.localTimeData.text = localTimeData
            binding.utcTimeData.text = utcTimeData

        }

    }



    private val updateTime = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            calculateTimeData()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTime)

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setWelcomeMessage() {
        val zoneId = ZoneId.of(timeZone)
        val zonedDateTime: ZonedDateTime = Instant.now().atZone(zoneId)
        when (zonedDateTime.hour) {
            in 1..12 -> {
                binding.titleText.text = getString(R.string.good_morning)
            }
            in 13..15 -> {
                binding.titleText.text = getString(R.string.good_after_noon)
            }
            in 15..19 -> {
                binding.titleText.text = getString(R.string.good_evening)
            }

            else -> {
                binding.titleText.text = getString(R.string.good_night)
            }
        }
    }





}