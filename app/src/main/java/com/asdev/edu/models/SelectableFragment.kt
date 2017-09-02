package com.asdev.edu.models

import android.support.v4.app.Fragment

abstract class SelectableFragment: Fragment() {

    /**
     * Called when this fragment is selected.
     */
    abstract fun onSelected()

    /**
     * Called when this fragment is reselected.
     */
    abstract fun onReselected()
}