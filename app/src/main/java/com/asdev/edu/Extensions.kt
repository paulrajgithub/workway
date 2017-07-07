package com.asdev.edu

import android.view.View

/**
 * Adjusts only the given paddings while retaining any unset ones.
 */
fun View.adjustPadding(left: Int = paddingLeft, top: Int = paddingTop, right: Int = paddingRight, bottom: Int = paddingBottom)
    = setPadding(left, top, right, bottom)
