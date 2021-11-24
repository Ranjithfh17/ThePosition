package com.fh.theposition.data.ui.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentCompassBinding
import com.fh.theposition.math.Angle.getAngle
import com.fh.theposition.math.Angle.normalizeEulerAngle
import com.fh.theposition.math.CompassAzimuth
import com.fh.theposition.math.LowPassFilter.smoothAndSetReadings
import com.fh.theposition.math.Vector3
import com.fh.theposition.util.AsyncImageLoader.loadImage
import com.fh.theposition.util.Direction.getDirectionNameFromAzimuth
import java.lang.StringBuilder
import java.util.*
import kotlin.math.abs


class Compass : Fragment(R.layout.fragment_compass), SensorEventListener {
    private lateinit var binding: FragmentCompassBinding

    private var handler = Handler(Looper.getMainLooper())
    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var showDirectionCode = true
    private var isUserRotatingDial = false
    private var isAnimated = true

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagnetometer: Sensor

    private var objectAnimator: ObjectAnimator? = null
    private var startAngle = 0F
    private var lastAngle = 0F
    private var readingsAlpha = 0.03F
    private var rotationAngle = 0f

    private var accelerometerReadings = FloatArray(3)
    private var magnetometerReadings = FloatArray(3)


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompassBinding.bind(view)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager


        try {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null &&
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null
            ) {
                haveAccelerometerSensor = false
                haveMagnetometerSensor = false

            } else {
                sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                haveAccelerometerSensor = true
                haveMagnetometerSensor = true
            }
        } catch (exception: Exception) {
            haveMagnetometerSensor = false
            haveAccelerometerSensor = false
        }




        loadImage(R.drawable.compass_dial, binding.dial, requireContext(), 0)
        binding.dialContainer.setOnTouchListener(DialTouchListener())
        setPhysical()

    }

    private fun setPhysical() {
        binding.dial.setPhysical(0F, 10F, 3000F)
    }

    private val compassDialAnimation = Runnable {
        objectAnimator = ObjectAnimator.ofFloat(
            binding.dial, "rotation",
            binding.dial.rotation, rotationAngle * -1
        )
        objectAnimator!!.duration = 1000L
        objectAnimator!!.interpolator = DecelerateInterpolator()
        objectAnimator!!.setAutoCancel(true)
        objectAnimator!!.addUpdateListener { animation ->
            viewRotation(abs(animation.getAnimatedValue("rotation") as Float), false)
        }
        objectAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {/*NO_OP*/}
            override fun onAnimationCancel(animation: Animator?) {/*NO_OP*/}
            override fun onAnimationRepeat(animation: Animator?) {/*NO_OP*/}
            override fun onAnimationEnd(animation: Animator?) {
                isUserRotatingDial = false
            }
        })
        objectAnimator!!.start()

    }

    private fun viewRotation(angle: Float, animate: Boolean) {
        binding.dial.rotationUpdate(angle * -1, animate)
        binding.degrees.text = StringBuilder()
            .append(abs(binding.dial.rotation.normalizeEulerAngle(true).toInt()))
            .append("°")

        binding.degreeText.text= getDirectionNameFromAzimuth(requireContext(),angle.toDouble()).toUpperCase(Locale.getDefault())

    }

    private inner class DialTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    isUserRotatingDial = true
                    objectAnimator?.removeAllListeners()
                    objectAnimator?.cancel()
                    binding.dial.clearAnimation()
                    handler.removeCallbacks(compassDialAnimation)
                    lastAngle = binding.dial.rotation
                    startAngle = getAngle(
                        event.x.toDouble(),
                        event.y.toDouble(),
                        binding.dialContainer.width.toFloat(),
                        binding.dialContainer.height.toFloat()
                    )
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val currentAngle = getAngle(
                        event.x.toDouble(),
                        event.y.toDouble(),
                        binding.dialContainer.width.toFloat(),
                        binding.dialContainer.height.toFloat()
                    )

                    val finalAngle = currentAngle - startAngle + lastAngle
                    viewRotation(abs(finalAngle.normalizeEulerAngle(true)), false)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    handler.postDelayed(compassDialAnimation, 1000)
                    return true
                }
            }

            return true
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {


        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                smoothAndSetReadings(accelerometerReadings, event.values, readingsAlpha)
                accelerometer = Vector3(
                    accelerometerReadings[0],
                    accelerometerReadings[1],
                    accelerometerReadings[2]
                )
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                smoothAndSetReadings(magnetometerReadings, event.values, readingsAlpha)
                magnetometer = Vector3(
                    magnetometerReadings[0],
                    magnetometerReadings[1],
                    magnetometerReadings[2]
                )
            }
        }

        if (!isUserRotatingDial) {
            rotationAngle =
                CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer)
            viewRotation(rotationAngle, isAnimated)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {/*NO_OP*/}

    private fun registerSensors() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregisterSensors() {
        if (haveMagnetometerSensor && haveAccelerometerSensor){
            sensorManager.unregisterListener(this,sensorMagnetometer)
            sensorManager.unregisterListener(this,sensorAccelerometer)
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensors()
    }

    override fun onPause() {
        super.onPause()
        unregisterSensors()
        handler.removeCallbacks(compassDialAnimation)
        objectAnimator?.removeAllListeners()
        objectAnimator?.cancel()
        binding.dial.clearAnimation()

    }

}


