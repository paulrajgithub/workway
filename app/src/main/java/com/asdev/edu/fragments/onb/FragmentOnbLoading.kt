package com.asdev.edu.fragments.onb

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R

/**
 * The tag associated with the [FragmentOnbLoading] fragment.
 */
const val FRAGMENT_ONB_LOADING = "FragmentOnbLoading"

/**
 * A fragment for the [OnBoardingActivity] which displays a loading UI.
 */
class FragmentOnbLoading: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onb_loading, container, false)
    }

}