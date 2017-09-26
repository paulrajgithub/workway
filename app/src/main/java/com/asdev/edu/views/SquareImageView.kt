package com.asdev.edu.views

import android.content.Context
import android.graphics.Canvas
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.AttributeSet
import android.widget.ImageView
import com.makeramen.roundedimageview.RoundedImageView

class SquareImageView(context: Context, attrs: AttributeSet): ImageView(context, attrs) {

    // square to width
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}
