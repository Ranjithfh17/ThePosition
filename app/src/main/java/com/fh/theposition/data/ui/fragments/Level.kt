package com.fh.theposition.data.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentLevelBinding
import com.fh.theposition.math.LowPassFilter.smoothAndSetReadings
import com.fh.theposition.math.MathExtensions.round
import com.fh.theposition.util.AsyncImageLoader.loadImage
import com.fh.theposition.util.HtmlFormatter.fromHtml


class Level : Fragment(R.layout.fragment_level), SensorEventListener {

    private lateinit var binding: FragmentLevelBinding

    private lateinit var gravity: Sensor
    private lateinit var sensorManager: SensorManager

    private val displayMetrics = DisplayMetrics()
    private val gravityReadings = FloatArray(3)
    private var hasGravitySensor = false
    private var isScreenTouched = false

    private var screenWidth = 0
    private var screenHeight = 0
    private val readingSAlpha = 0.01f

    private var gravityMotionWidthCompensator = 0f
    private var gravityMotionHeightCompensator = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLevelBinding.bind(view)




        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            hasGravitySensor = true

        } else {
            hasGravitySensor = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(displayMetrics)
        } else {
            requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels


        gravityMotionHeightCompensator = screenHeight / 19.6F
        gravityMotionWidthCompensator = screenWidth / 19.6F


        setStyle()


        binding.apply {
            boundingBox.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        levelIndicator.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        levelDot.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }

                    MotionEvent.ACTION_UP -> {
                        levelIndicator.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        levelDot.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }
                }
                true
            }
        }
    }


    private fun setStyle() {
        loadImage(R.drawable.level_indicator, binding.levelIndicator, requireContext(), 0)
        loadImage(R.drawable.level_dot, binding.levelDot, requireContext(), 0)
    }

    private fun registerSensor() {
        if (hasGravitySensor) {
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST)
        }

    }


    private fun unRegisterSensor() {
        sensorManager.unregisterListener(this, gravity)

    }

    override fun onResume() {
        super.onResume()
        registerSensor()
    }

    override fun onPause() {
        super.onPause()
        unRegisterSensor()
        binding.apply {
            levelIndicator.clearAnimation()
            levelDot.clearAnimation()
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {

            if (isScreenTouched) return

            smoothAndSetReadings(gravityReadings, event.values, readingSAlpha)

            binding.levelDot.translationX =
                binding.levelIndicator.translationX * -0.3f //gravityReadings[0] * -1 * gravityWidthMotionCompensator / 4
            binding.levelDot.translationY =
                binding.levelIndicator.translationY * -0.3f //gravityReadings[1] * gravityHeightMotionCompensator / 4

            // level_dot.scaleX = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)
            // level_dot.scaleY = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)

            if (gravityReadings[0] in -0.2..0.2 && gravityReadings[1] in -0.2..0.2) {
                binding.levelDot.imageTintList = ColorStateList.valueOf(Color.parseColor("#005BFF"))
            } else {
                binding.levelDot.imageTintList = ColorStateList.valueOf(Color.parseColor("#BF4848"))
            }

            if (gravityReadings[0] * gravityMotionWidthCompensator - binding.levelIndicator.width / 2
                > binding.boundingBox.width / 2 * -1
                && gravityReadings[0] * gravityMotionWidthCompensator + binding.levelIndicator.width / 2
                < binding.boundingBox.width / 2
            ) {
                binding.levelIndicator.translationX =
                    gravityReadings[0] * gravityMotionWidthCompensator
            }

            if (gravityReadings[1] * -1 * gravityMotionHeightCompensator - binding.levelIndicator.height / 2
                > binding.boundingBox.height / 2 * -1
                && gravityReadings[1] * -1 * gravityMotionHeightCompensator + binding.levelIndicator.height / 2
                < binding.boundingBox.height / 2
            ) {
                binding.levelIndicator.translationY =
                    gravityReadings[1] * -1 * gravityMotionHeightCompensator
            }

            binding.levelX.text =
                fromHtml("<b>X:</b> ${round(gravityReadings[0].toDouble(), 2)} m/s²")
            binding.levelY.text =
                fromHtml("<b>Y:</b> ${round(gravityReadings[1].toDouble(), 2)} m/s²")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}