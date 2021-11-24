package com.fh.theposition.data.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentSettingsBinding
import com.fh.theposition.databinding.LayoutThemeBinding
import com.fh.theposition.util.ThemeSetter
import com.fh.theposition.viewmodels.MainPrefViewModel
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class Settings : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var themeBinding: LayoutThemeBinding
    private val viewModel by activityViewModels<MainPrefViewModel>()
    private lateinit var versionName: String

    private lateinit var alertDialog: AlertDialog

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        alertDialog = AlertDialog.Builder(requireContext()).create()
        val layout = layoutInflater.inflate(R.layout.layout_theme, null)
        themeBinding = LayoutThemeBinding.bind(layout)
        alertDialog.setView(layout)


        showThemeDialog()
        getTheme()

        setKeepScreenOn()
        isScreenEnabled()

        binding.navigateBack.setOnClickListener {
            findNavController().popBackStack()
        }

        try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            versionName = packageInfo.versionName
            binding.appVersion.text = versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        binding.updateApp.setOnClickListener {
            checkAppUpdate()
        }


    }

    private fun setKeepScreenOn() {
        binding.keepScreenLayout.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                if (binding.screenEnableDisable.text.toString() == "Enable") {
                    viewModel.setScreenOn("isScreenOn", true)
                    isScreenEnabled()
                } else {
                    viewModel.setScreenOn("isScreenOn", false)
                    isScreenEnabled()
                }
            }
        }
    }

    private fun isScreenEnabled() {
        lifecycleScope.launchWhenStarted {
            viewModel.isScreenOn("isScreenOn").collect { enabled ->
                if (enabled) {
                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    binding.screenEnableDisable.text = getString(R.string.disable)
                    binding.keepScreenLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.enable_back)
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    binding.screenEnableDisable.text = getString(R.string.enable)
                    binding.keepScreenLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.disable_back)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showThemeDialog() {
        binding.themeLayout.setOnClickListener {
            setThemeDialog()
        }

    }


    @SuppressLint("InflateParams")
    private fun setThemeDialog() {
        alertDialog.show()

        themeBinding.lightTheme.setOnClickListener {
            setTheme("lightTheme")
            alertDialog.dismiss()

        }
        themeBinding.darKTheme.setOnClickListener {
            setTheme("darkTheme")
            alertDialog.dismiss()

        }
        themeBinding.followSystem.setOnClickListener {
            setTheme("followSystem")
            alertDialog.dismiss()

        }
        themeBinding.auto.setOnClickListener {
            setTheme("auto")
            alertDialog.dismiss()

        }
    }

    private fun setTheme(value: String) {
        lifecycleScope.launchWhenStarted {
            viewModel.setTheme("theme", value)
            getTheme()
        }
    }

    private fun checkAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        val appUpdateInfo = appUpdateManager.appUpdateInfo

        appUpdateInfo.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.FLEXIBLE, requireActivity(), 1001)
            } else {
                Toast.makeText(requireContext(), "No Update Available", Toast.LENGTH_SHORT).show()
            }
        }

             appUpdateInfo.addOnFailureListener{
                  Log.i("TAG", "checkAppUpdate:${it.localizedMessage} ")
             }
    }


    private fun getTheme() {
        lifecycleScope.launchWhenStarted {
            viewModel.getTheme("theme").collect { themeMode ->
                Log.i("TAG", "getTheme: $themeMode")
                ThemeSetter.setAppTheme(themeMode)

                themeBinding.lightTheme.isChecked = themeMode == "lightTheme"
                themeBinding.darKTheme.isChecked = themeMode == "darkTheme"
                themeBinding.followSystem.isChecked = themeMode == "followSystem"
                themeBinding.auto.isChecked = themeMode == "auto"

                when (themeMode) {
                    "lightTheme" -> {
                        binding.themeName.text = getString(R.string.lightTheme)
                    }
                    "darkTheme" -> {
                        binding.themeName.text = getString(R.string.darkTheme)
                    }

                    "followSystem" -> {
                        binding.themeName.text = getString(R.string.followSystem)
                    }
                    "auto" -> {
                        binding.themeName.text = getString(R.string.auto)
                    }
                }

            }
        }
    }


}