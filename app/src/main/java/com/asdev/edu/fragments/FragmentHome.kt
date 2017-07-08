package com.asdev.edu.fragments

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.MD_PRIMARY_COLOR_REFS
import com.asdev.edu.R
import com.asdev.edu.RANDOM
import com.asdev.edu.models.DCourse

class FragmentHome: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        // add 7 of them
        for(i in 0..7) {
            VHCourseSelector.inflate(DCourse.values()[RANDOM.nextInt(DCourse.values().size)], courseGrid)
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

}

class VHCourseSelector private constructor(val view: View) {

    fun setToCourse(course: DCourse) {
        // customize it
        val iconView = view.findViewById(R.id.course_icon) as ImageView
        val titleView = view.findViewById(R.id.course_title) as TextView

        // set the title text
        titleView.text = course.getUITitle(view.context)
        // set the circle color
        iconView.background.setColorFilter(ContextCompat.getColor(view.context, course.colorRes), PorterDuff.Mode.SRC_ATOP)
        iconView.setImageResource(course.iconRes)
    }

    companion object {

        /**
         * Creates a new course selector view item.
         */
        fun inflate(course: DCourse, grid: GridLayout): VHCourseSelector {
            // inflate a new view
            val inflater = LayoutInflater.from(grid.context)
            val v = inflater.inflate(R.layout.item_course_selector, grid, false)

            // apply the params to the view
            val params = v.layoutParams as GridLayout.LayoutParams
            // set to column weight of 1
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, GridLayout.FILL, 1f)

            val holder = VHCourseSelector(v)
            holder.setToCourse(course)

            // add to grid
            grid.addView(v, v.layoutParams)

            return holder
        }

    }

}