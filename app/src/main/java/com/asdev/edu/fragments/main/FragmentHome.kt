package com.asdev.edu.fragments.main

import android.content.Context
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asdev.edu.R
import com.asdev.edu.adapters.AdapterHomeUpdates
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.DUser
import com.asdev.edu.models.SelectableFragment
import com.asdev.edu.models.SharedData
import com.asdev.edu.views.VHCourseSelector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A fragment for the [MainActivity] which displays the home landing content.
 */
class FragmentHome: SelectableFragment() {

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val autoimport1 = view.findViewById(R.id.autoimport_image1) as ImageView
        val autoimport2 = view.findViewById(R.id.autoimport_image2) as ImageView
        val autoimport3 = view.findViewById(R.id.autoimport_image3) as ImageView

        val appbar = view.findViewById(R.id.appbar)
        val content = view.findViewById(R.id.home_content) as NestedScrollView
        // hide the toolbar shadow when the scrollview is at the top
        content.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY == 0) {
                // hide shadow
                ViewCompat.setElevation(appbar, 0f)
            } else {
                // show shadow
                ViewCompat.setElevation(appbar, 12f)
            }
        }

        // configure the recycler
        val updateRecycler = view.findViewById(R.id.home_update_recycler) as RecyclerView
        // disable nested scrolling for smooth scroll
        ViewCompat.setNestedScrollingEnabled(updateRecycler, false)
        updateRecycler.layoutManager = LinearLayoutManager(context)
        updateRecycler.setHasFixedSize(true)

        // create a listening subject
        val subject = BehaviorSubject.create<DUIAction<String>>()

        // bind the handler to the subject
        val sub = subject.observeOn(AndroidSchedulers.mainThread())
                .subscribe(postActionHandler)

        subscriptions.add(sub)

        // set the adapter
        updateAdapter = AdapterHomeUpdates(subject)
        updateRecycler.adapter = updateAdapter

        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        val duser = SharedData.duserRo(context)
        // try and use the duser courses, otherwise the default courses
        val courses = duser?.starredCourses ?: DUser.blank().starredCourses

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