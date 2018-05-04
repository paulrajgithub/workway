package com.asdev.edu.fragments.main

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.asdev.edu.R
import com.asdev.edu.adapters.AdapterHomeUpdates
import com.asdev.edu.getCoursesInPriority
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SelectableFragment
import com.asdev.edu.models.SharedData
import com.asdev.edu.views.VHCourseSelector
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A fragment for the [MainActivity] which displays the home landing content.
 */
class FragmentHome: SelectableFragment() {

    private val COURSE_SHOW_COUNT_MIN = 8

    /**
     * The adapter for the home updates content.
     */
    private lateinit var updateAdapter: AdapterHomeUpdates

    /**
     * Listens to any post action events.
     */
    private val postActionHandler: (DUIAction<String>) -> Unit = {
        Log.d("FragmentHome", "Got DUIAction: $it")
    }

    private var subscriptions = CompositeDisposable()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // TODO: request the feed from the server
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout

        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        (view.findViewById(R.id.header) as LinearLayout).bringToFront()

//        // configure the recycler
//        // val updateRecycler = view.findViewById(R.id.home_update_recycler) as RecyclerView
//        // disable nested scrolling for smooth scroll
//        // ViewCompat.setNestedScrollingEnabled(updateRecycler, false)
//        // updateRecycler.layoutManager = LinearLayoutManager(context)
//        // updateRecycler.setHasFixedSize(true)
//
//        // create a listening subject
//        val subject = BehaviorSubject.create<DUIAction<String>>()
//
//        // bind the handler to the subject
//        val sub = subject.observeOn(AndroidSchedulers.mainThread())
//                .subscribe(postActionHandler)
//
//        subscriptions.add(sub)
//
//        // set the adapter
//        updateAdapter = AdapterHomeUpdates(subject)
//        // updateRecycler.adapter = updateAdapter
//
//        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        val duser = SharedData.duserRo(context!!)

        // show at least 8 courses or at least the total number of starred courses, with the starred courses being first priority
        val courses = getCoursesInPriority(duser).take(maxOf(COURSE_SHOW_COUNT_MIN, duser?.starredCourses?.size?: 0))

        for(course in courses) {
            VHCourseSelector.inflate(course, courseGrid)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.dispose()
        subscriptions = CompositeDisposable()
    }

    override fun onSelected() {
    }

    override fun onReselected() {
        // scroll the layout to the top
        home_content.fullScroll(View.FOCUS_UP)
    }
}