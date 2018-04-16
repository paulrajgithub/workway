package com.asdev.edu.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DCourse
import com.asdev.edu.views.VHCourseSelector

/**
 * A [RecyclerView] adapter which binds the given list of [DCourse]s as [VHCourseSelector]
 * items.
 */
class CoursesAdapter(private val courses: List<DCourse>, private val onClickListener: ((DCourse) -> Unit)?): RecyclerView.Adapter<VHCourseSelector>() {

    /**
     * The bound courses to this adapter.
     */
    override fun onBindViewHolder(holder: VHCourseSelector, position: Int) {
        holder.setToCourse(courses[position])
        holder.onClickListener = onClickListener
    }

    override fun getItemCount() = courses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHCourseSelector {
        // create a VHCourse selector layout
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_course_selector, parent, false)
        return VHCourseSelector(view)
    }

}