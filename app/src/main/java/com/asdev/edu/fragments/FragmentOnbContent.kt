package com.asdev.edu.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

const val FRAGMENT_ONB_CONTENT = "FragmentOnbContent"

class FragmentOnbContent: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_onb_content, container, false)
        return view
    }

}