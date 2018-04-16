package com.asdev.edu.fragments.onb

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

/**
 * The tag associated with the [FragmentOnbProfile] fragment.
 */
const val FRAGMENT_ONB_PROFILE = "FragmentOnbProfile"

/**
 * A fragment for the [OnBoardingActivity] which displays the profile entry input.
 */
class FragmentOnbProfile: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onb_profile, container, false)
        return view
    }

}