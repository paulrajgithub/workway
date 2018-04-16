package com.asdev.edu.fragments.main

import android.os.Bundle
import android.view.*
import com.asdev.edu.R
import com.asdev.edu.models.SelectableFragment
import kotlinx.android.synthetic.main.fragment_collections.*

/**
 * A fragment for the [MainActivity] which displays the user's collections.
 */
class FragmentCollections: SelectableFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onSelected() {
    }

    override fun onReselected() {
    }

}