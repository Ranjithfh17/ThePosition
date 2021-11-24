package com.fh.theposition.data.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.fh.theposition.R
import com.fh.theposition.callbacks.OnMapClickListener
import com.fh.theposition.data.ui.fragments.Map
import com.fh.theposition.databinding.ActivityMainBinding
import com.fh.theposition.services.FusedLocationService
import com.fh.theposition.util.Constants.ACTION_START_FUSED_SERVICE
import com.fh.theposition.util.Constants.ACTION_STOP_FUSED_SERVICE
import com.fh.theposition.util.Constants.PERMISSION_REQUEST_CODE
import com.fh.theposition.util.Constants.START_MAP_FRAGMENT
import com.fh.theposition.util.LocationUtil.displayLocationSettingsRequest
import com.fh.theposition.util.LocationUtil.getLocationStatus
import com.fh.theposition.util.ThemeSetter
import com.fh.theposition.viewmodels.MainPrefViewModel
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel by viewModels<MainPrefViewModel>()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        MobileAds.initialize(this)

        checkRunTimePermission()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        navigateToMapFragment(intent)
        setTheme()
        setScreenEnabled()



        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home2, R.id.clock, R.id.compass, R.id.level, R.id.map2 -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
            }
        }

        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {/*NO-OP*/ }



    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToMapFragment(intent)
    }

    private fun navigateToMapFragment(intent: Intent?) {
        if (intent?.action == START_MAP_FRAGMENT) {
            navController.navigate(R.id.service_to_map_fragment)
        }
    }

    private fun setTheme() {
        lifecycleScope.launchWhenStarted {
            viewModel.getTheme("theme").collect {
                ThemeSetter.setAppTheme(it)
            }
        }
    }

    private fun setScreenEnabled() {
        lifecycleScope.launchWhenStarted {
            viewModel.isScreenOn("isScreenOn").collect { enabled ->
                if (enabled){
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

    }


    private fun checkRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            grantResult()
        } else {
            showLocationServicePermission()
        }

    }

    private fun grantResult() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService()
                showLocationServicePermission()
                Log.i("TAG", "onRequestPermissionsResult: perm granted ")
            } else {
                showDialogAlert()
            }
        }
    }

    private fun showLocationServicePermission() {
        if (!getLocationStatus(this)) {
            displayLocationSettingsRequest(this, this)
        }
    }

    private fun startLocationService() {

        Intent(this, FusedLocationService::class.java).also {
            it.action = ACTION_START_FUSED_SERVICE
            startService(it)
        }


    }

    private fun stopLocationServices() {

        Intent(this, FusedLocationService::class.java).also {
            it.action = ACTION_STOP_FUSED_SERVICE
            startService(it)
        }


    }

    private fun showDialogAlert() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs location permission to use map feature. You should grant them in  the app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(""
        ) { _, _ ->/*NO-OP*/ }
        builder.setCancelable(false)
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null))
        startActivity(intent)
    }



    override fun onResume() {
        super.onResume()
        startLocationService()
    }

    override fun onPause() {
        super.onPause()
        stopLocationServices()
    }


}