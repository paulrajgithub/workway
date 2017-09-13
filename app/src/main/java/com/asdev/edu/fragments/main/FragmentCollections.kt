package com.asdev.edu.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.SelectableFragment

/**
 * A fragment for the [MainActivity] which displays the user's collections.
 */
class FragmentCollections: SelectableFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onSelected() {
    }

    override fun onReselected() {
    }

}