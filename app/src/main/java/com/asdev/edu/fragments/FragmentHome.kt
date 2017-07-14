package com.asdev.edu.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asdev.edu.R
import com.asdev.edu.RANDOM
import com.asdev.edu.adapters.AdapterHomeUpdates
import com.asdev.edu.models.DCourse
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.PostId
import com.asdev.edu.views.VHCourseSelector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

const val SPAN_HIGHLIGHT_FIRST = true
const val SPAN_BALANCE_LAST = true
const val SPAN_FULL = 2
const val SPAN_ONE = 1

class FragmentHome: Fragment() {

    /**
     * The adapter for the home updates content.
     */
    private lateinit var updateAdapter: AdapterHomeUpdates

    /**
     * Listens to any post action events.
     */
    private val postActionHandler: (DUIAction<PostId>) -> Unit = {
        Log.d("FragmentHome", "Got DUIAction: $it")
        // TODO: this holds an implict reference to this, so memory leaks?
    }

    private var subscriptions = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

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
        // set to a 2 column grid layout
        updateRecycler.layoutManager = GridLayoutManager(context, SPAN_FULL).apply {
            // TODO: adaptive span sizes
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val size = updateAdapter.itemCount

                    // highlight the first item if in config
                    if(position == 0 && SPAN_HIGHLIGHT_FIRST) {
                        return SPAN_FULL
                    } else if(position == size - 1 && SPAN_BALANCE_LAST) {
                        // if highlight first, shift items mod by one to account for first 2 span
                        if(SPAN_HIGHLIGHT_FIRST) {
                            return if((size - 1) % 2 == 1) SPAN_FULL else SPAN_ONE
                        } else {
                           return if(size % 2 == 1) SPAN_FULL else SPAN_ONE
                        }
                    }

                    // default 1 item span
                    return SPAN_ONE
                }
            }
        }

        // create a listening subject
        val subject = BehaviorSubject.create<DUIAction<PostId>>()

        // bind the handler to the subject
        val sub = subject.observeOn(AndroidSchedulers.mainThread())
                .subscribe(postActionHandler)

        subscriptions.add(sub)

        // set the adapter
        updateAdapter = AdapterHomeUpdates(subject)
        updateRecycler.adapter = updateAdapter

        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        val courses = DCourse.values().filter { RANDOM.nextBoolean() }

        // TODO: get starred courses, something like:
        // val courses = user.starredCourses //
        for(course in courses) {
            VHCourseSelector.inflate(course, courseGrid)
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.dispose()
    }

}