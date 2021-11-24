package com.fh.theposition.data.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fh.theposition.databinding.ActivityWelcomeBinding
import com.fh.theposition.viewmodels.MainPrefViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity(){

    private lateinit var binding: ActivityWelcomeBinding
    private val viewModel by viewModels<MainPrefViewModel>()



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.setOnClickListener {

                if (binding.name.text.toString().isNotBlank()){
                   navigateToHome()
                }else{
                    Toast.makeText(this, "Please type your name", Toast.LENGTH_SHORT).show()
                }
        }
    }





    private fun navigateToHome() {
        viewModel.saveName("name", binding.name.text.toString())
        viewModel.saveLoginInfo("isFirstTime", true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }





}