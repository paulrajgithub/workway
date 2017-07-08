package com.asdev.edu.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asdev.edu.R
import com.asdev.edu.RANDOM
import com.asdev.edu.models.DCourse
import com.asdev.edu.views.VHCourseSelector

class FragmentHome: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val autoimport1 = view.findViewById(R.id.autoimport_image1) as ImageView
        val autoimport2 = view.findViewById(R.id.autoimport_image2) as ImageView
        val autoimport3 = view.findViewById(R.id.autoimport_image3) as ImageView

        autoimport1.setImageResource(R.drawable.work)
        autoimport2.setImageResource(R.drawable.work2)
        autoimport3.setImageResource(R.drawable.work3)

        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        // TODO: get starred courses
        val courses = DCourse.values().filter { RANDOM.nextBoolean() }
        for(course in courses) {
            VHCourseSelector.inflate(course, courseGrid)
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

}