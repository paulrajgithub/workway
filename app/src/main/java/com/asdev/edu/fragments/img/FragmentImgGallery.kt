package com.asdev.edu.fragments.img

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

class FragmentImgGallery: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inflater?: return null // assert that the inflater is not null

        return inflater.inflate(R.layout.fragment_img_gallery, container, false)
    }

}