package com.asdev.edu.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

const val FRAGMENT_ONB_LOADING = "FragmentOnbLoading"

class FragmentOnbLoading: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(inflater == null)
            return null

        return inflater.inflate(R.layout.fragment_onb_loading, container, false)
    }

}