package com.asdev.edu.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.ImageActivity
import com.asdev.edu.R
import com.asdev.edu.RC_IMAGE_ACTIVITY
import com.asdev.edu.models.SelectableFragment

class FragmentCreate : SelectableFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    @Volatile
    private var showImageActivity = false

    override fun onSelected() {
        showImageActivity = true
    }

    override fun onReselected() {
    }

    override fun onResume() {
        super.onResume()

        if(showImageActivity) {
            // show the image selection activity
            startActivityForResult(Intent(context, ImageActivity::class.java), RC_IMAGE_ACTIVITY)
            showImageActivity = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_IMAGE_ACTIVITY) {

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}