package com.asdev.edu.models

import android.support.v4.app.Fragment

/**
 * A fragment which may be selected by navigational
 * components.
 */
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