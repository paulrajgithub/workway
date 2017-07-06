package com.asdev.edu.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

class FragmentCreate: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_create, container, false)
        return view
    }

}