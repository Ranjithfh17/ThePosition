package com.fh.theposition.data.ui.dialog

import android.os.Bundle
import android.view.View
import com.fh.theposition.R

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class CustomBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
        dialog?.window?.setDimAmount(0.3f)
    }
}