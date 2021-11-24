package com.fh.theposition.data.ui.dialog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.fh.theposition.R
import com.fh.theposition.databinding.FragmentMapTypeDialogBinding
import com.fh.theposition.viewmodels.MainPrefViewModel
import kotlinx.coroutines.flow.collect


class MapTypeDialog : CustomBottomSheetDialog() {

    private lateinit var binding:FragmentMapTypeDialogBinding
    private val viewModel by activityViewModels<MainPrefViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentMapTypeDialogBinding.inflate(inflater,container,false)

        getMapType()

        binding.typeNormal.setOnClickListener {
            setMapType("Normal")

            dialog?.dismiss()
        }

        binding.typeHybrid.setOnClickListener {
            setMapType("Hybrid")

            dialog?.dismiss()
        }
        binding.typeTerrain.setOnClickListener {
            setMapType("Terrain")

            dialog?.dismiss()
        }

        binding.typeSatellite.setOnClickListener {
            setMapType("Satellite")

            dialog?.dismiss()
        }


        return binding.root


    }

    private fun setMapType(value:String){
        viewModel.setMapType("type",value)
        getMapType()

    }

    private fun getMapType(){
        lifecycleScope.launchWhenStarted {
            viewModel.getMapType("type").collect { type ->


                when(type){
                    "Normal" -> {
                        binding.typeNormal.isChecked=true
                    }

                    "Hybrid" -> {
                        binding.typeHybrid.isChecked=true
                    }

                    "Terrain" -> {
                        binding.typeTerrain.isChecked=true
                    }
                    "Satellite" -> {
                        binding.typeSatellite.isChecked=true
                    }
                }

            }
        }
    }




}