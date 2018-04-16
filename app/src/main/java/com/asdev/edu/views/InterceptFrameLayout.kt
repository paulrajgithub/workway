package com.asdev.edu.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout

class InterceptFrameLayout(context: Context, attrs: AttributeSet): FrameLayout(context, attrs), GestureDetector.OnGestureListener {

    private val detector = GestureDetector(context, this)

    override fun onInterceptTouchEvent(ev: MotionEvent?) = true

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        detector.onTouchEvent(event)
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean = true

    override fun onDown(e: MotionEvent?): Boolean = true

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        println("Fling: $velocityX")
        return true
    }

    private var totalScroll = 0f
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        totalScroll += distanceX

        // update translation
        translationX -= distanceX
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

}