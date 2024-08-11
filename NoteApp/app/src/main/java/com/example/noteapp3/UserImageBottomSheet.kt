package com.example.noteapp3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserImageBottomSheet : BottomSheetDialogFragment() {

    interface OnImageOptionClickListener {
        fun onViewImage()
        fun onChangeImageFromCamera()
        fun onChangeImageFromStorage()
    }

    private var listener: OnImageOptionClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_options_bottom_sheet, container, false)

        view.findViewById<View>(R.id.view_image).setOnClickListener {
            listener?.onViewImage()
            dismiss()
        }

        view.findViewById<View>(R.id.change_image_camera).setOnClickListener {
            listener?.onChangeImageFromCamera()
            dismiss()
        }

        view.findViewById<View>(R.id.change_image_storage).setOnClickListener {
            listener?.onChangeImageFromStorage()
            dismiss()
        }

        return view
    }

    fun setOnImageOptionClickListener(listener: OnImageOptionClickListener) {
        this.listener = listener
    }
}
