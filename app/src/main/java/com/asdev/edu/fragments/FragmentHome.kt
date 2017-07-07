package com.asdev.edu.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.MD_PRIMARY_COLOR_REFS
import com.asdev.edu.R
import com.asdev.edu.RANDOM
import com.asdev.edu.adjustPadding
import com.asdev.edu.models.DCourse

class FragmentHome: Fragment() {

    private val contentAdapter = AdapterHomeContent()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val appbar = view.findViewById(R.id.appbar) as AppBarLayout

        val recycler = view.findViewById(R.id.home_recycler) as RecyclerView
        // make the content as a grid with 6 width
        val grid = GridLayoutManager(context, AdapterHomeContent.SPAN_FULL)
        grid.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = contentAdapter.getSpanSize(position)
        }
        recycler.layoutManager = grid
        // set the adapter as well
        recycler.adapter = contentAdapter

        // add listener to hide shadow when it hits the top
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val scrollAmt = recycler.computeVerticalScrollOffset()
                // if 0, hide shadow, otherwise show it
                ViewCompat.setElevation(appbar, if(scrollAmt == 0) 0f else 12f)
            }
        })

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

}

class AdapterHomeContent: RecyclerView.Adapter<VHHomeItem>() {

    companion object {
        const val VT_BLANK_HEADER = 0
        const val VT_TEXT_HEADER = 1
        const val VT_MORE_BUTTON = 2
        const val VT_AUTOIMPORT_ITEM = 3
        const val VT_UPDATE_ITEM = 4
        const val VT_DIVIDER = 5
        const val VT_COURSE_ITEM = 6

        const val SPAN_FULL = 12
        const val SPAN_AUTOIMPORT_ITEM = 4
        const val SPAN_UPDATE_ITEM = SPAN_FULL
        const val SPAN_COURSE_ITEM = 3
        const val SPAN_COURSE_ITEM_LARGE = 4

        const val HIGHLIGHT_COURSE_END_INDEX = 3

        ////// View index functions //////
        fun headerBlankIndex() = -1
        fun headerAutoImportIndex()= headerBlankIndex() + 1
        fun itemsAutoImportIndex() = headerAutoImportIndex() + 1..headerAutoImportIndex() + 3 // TODO: dynamic number of items
        fun buttonAutoImportMoreIndex() = itemsAutoImportIndex().endInclusive + 1
        fun dividerAutoImportIndex() = buttonAutoImportMoreIndex() + 1

        fun headerCoursesIndex() = dividerAutoImportIndex() + 1
        fun itemsCoursesIndex() = headerCoursesIndex() + 1..headerCoursesIndex() + 7 // TODO: dynamic
        fun buttonCoursesMoreIndex() = itemsCoursesIndex().endInclusive + 1
        fun dividerCoursesIndex() = buttonCoursesMoreIndex() + 1

        fun headerUpdatesIndex() = dividerCoursesIndex() + 1
        fun itemsUpdatesIndex() = headerUpdatesIndex() + 1..headerUpdatesIndex() + 4 // TODO: dynamic number of items
    }

    /*
    The layout is as follows:
    0: Blank Header                - 6 cols
    1: Text header - Auto-import   - 6 cols
    2-4: Autoimport items          - 2 cols each
    5: More button                 - 6 cols
    6: Divider                     - 6 cols
    7: Text header - Updates       - 6 cols
    8-x: Updates item             - 3 cols each
     */

    override fun onBindViewHolder(holder: VHHomeItem?, position: Int) {
        holder?.bind(position)
    }

    override fun getItemViewType(position: Int) =
        when(position) {
            headerBlankIndex() -> VT_BLANK_HEADER
            headerAutoImportIndex() -> VT_TEXT_HEADER
            in itemsAutoImportIndex() -> VT_AUTOIMPORT_ITEM
            buttonAutoImportMoreIndex() -> VT_MORE_BUTTON
            dividerAutoImportIndex() -> VT_DIVIDER

            headerCoursesIndex() -> VT_TEXT_HEADER
            in itemsCoursesIndex() -> VT_COURSE_ITEM
            buttonCoursesMoreIndex() -> VT_MORE_BUTTON
            dividerCoursesIndex() -> VT_DIVIDER

            headerUpdatesIndex() -> VT_TEXT_HEADER
            in itemsUpdatesIndex() -> VT_UPDATE_ITEM
            else -> -1
        }

    override fun getItemCount() = itemsUpdatesIndex().endInclusive + 1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VHHomeItem {
        val inflater = LayoutInflater.from(parent?.context)

        val layoutRes = when(viewType) {
            VT_BLANK_HEADER -> R.layout.header_blank
            VT_MORE_BUTTON -> R.layout.item_more_button
            VT_TEXT_HEADER -> R.layout.item_text_header
            VT_UPDATE_ITEM -> R.layout.item_update_item
            VT_DIVIDER -> R.layout.item_divider
            VT_AUTOIMPORT_ITEM -> R.layout.item_auto_import
            VT_COURSE_ITEM -> R.layout.item_course_selector
            else -> -1
        }

        val view = inflater.inflate(layoutRes, parent, false)
        return VHHomeItem(view, viewType)
    }

    fun getSpanSize(position: Int): Int {
        val type = getItemViewType(position)

        return when(type) {
            VT_BLANK_HEADER, VT_TEXT_HEADER, VT_MORE_BUTTON, VT_DIVIDER -> SPAN_FULL
            VT_AUTOIMPORT_ITEM -> SPAN_AUTOIMPORT_ITEM
            VT_UPDATE_ITEM -> SPAN_UPDATE_ITEM
            VT_COURSE_ITEM -> {
                // if part of highlight, make smaller
                val posRelative = position - itemsCoursesIndex().start
                if(posRelative in 0..HIGHLIGHT_COURSE_END_INDEX) {
                    SPAN_COURSE_ITEM
                } else {
                    SPAN_COURSE_ITEM_LARGE
                }

            }
            else -> SPAN_FULL
        }
    }

}

val WORK_ITEMS = intArrayOf(R.drawable.work, R.drawable.work2, R.drawable.work3) // TODO: delete ui testing code

class VHHomeItem(itemView: View, val type: Int): RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int) {
        // check if we need to set the text of headers
        if(type == AdapterHomeContent.VT_TEXT_HEADER) {
            when (position) {
                AdapterHomeContent.headerAutoImportIndex() -> {
                    // auto import text
                    // the item view is a text view
                    val textView = itemView as TextView
                    textView.text = itemView.context.getText(R.string.text_auto_import)
                    // set background as blue
                    itemView.setBackgroundResource(R.drawable.full_accent)
                    // set color as white
                    textView.setTextColor(Color.WHITE)
                }
                AdapterHomeContent.headerUpdatesIndex() -> {
                    // updates text
                    // the item view is a text view
                    val textView = itemView as TextView
                    textView.text = itemView.context.getText(R.string.text_updates)
                }
                AdapterHomeContent.headerCoursesIndex() -> {
                    val textView = itemView as TextView
                    textView.text = itemView.context.getText(R.string.text_find_by_course)
                }
            }
        } else if(type == AdapterHomeContent.VT_AUTOIMPORT_ITEM) {
            // set to a random work item
            val workRes = WORK_ITEMS[position - AdapterHomeContent.itemsAutoImportIndex().start] // TODO
            val img = itemView.findViewById(R.id.image) as ImageView // TODO
            img.setImageResource(workRes) // TODO

            // set gutters
            val gutterSize = itemView.context.resources.getDimensionPixelSize(R.dimen.gutter_size)
            // if first auto import item, then set left gutter
            if(position == AdapterHomeContent.itemsAutoImportIndex().start) {
                // set the left margin as gutter
                setGutter(left = true, size = gutterSize)
            } else if(position == AdapterHomeContent.itemsAutoImportIndex().endInclusive) {
                // we are the last auto import item, so set margin right
                setGutter(right = true, size = gutterSize)
            }
        } else if(type == AdapterHomeContent.VT_UPDATE_ITEM) {
            // get the position relative to the start of the update items
            val positionRelative = position - AdapterHomeContent.itemsUpdatesIndex().start

            val ppView = itemView.findViewById(R.id.update_profile_photo) as ImageView
            val workRes = WORK_ITEMS[positionRelative % WORK_ITEMS.size] // TODO
            val img = itemView.findViewById(R.id.update_image) as ImageView // TODO
            img.setImageResource(workRes) // TODO

            // if index is even, it means its on the left
            // otherwise its on the right
            if(positionRelative % 2 == 0) {
                ppView.setImageResource(R.drawable.pp1) // TODO
            } else {
                ppView.setImageResource(R.drawable.pp2) // TODO
            }
        } else if(type == AdapterHomeContent.VT_COURSE_ITEM) {
            val gutterSize = itemView.context.resources.getDimensionPixelSize(R.dimen.gutter_size)
            val nominalSize = itemView.context.resources.getDimensionPixelSize(R.dimen.course_item_padding_horiz)

            val posRelative = position - AdapterHomeContent.itemsCoursesIndex().start

            // TODO: TEMP UI HACKS
            val imgView = itemView.findViewById(R.id.course_icon) as ImageView
            // change bg color
            imgView.background.setColorFilter(
                    ContextCompat.getColor(itemView.context, MD_PRIMARY_COLOR_REFS[posRelative % MD_PRIMARY_COLOR_REFS.size]),
                    PorterDuff.Mode.SRC_ATOP
            )
            // get a random course
            val course = DCourse.values()[RANDOM.nextInt(DCourse.values().size)]
            // set title and icon to that
            val titleView = itemView.findViewById(R.id.course_title) as TextView
            titleView.text = course.getUITitle(itemView.context)
            imgView.setImageResource(course.iconRes)

            // if part of highlight, mod differently
            if(posRelative in 0..AdapterHomeContent.HIGHLIGHT_COURSE_END_INDEX) {
                if(posRelative == 0) {
                    // left gutter
                    itemView.adjustPadding(left = gutterSize * 2, right = nominalSize)
                } else if(posRelative == AdapterHomeContent.HIGHLIGHT_COURSE_END_INDEX) {
                    // right gutter
                    itemView.adjustPadding(right = gutterSize * 2, left = nominalSize)
                } else {
                    // default padding
                    itemView.adjustPadding(left = nominalSize, right = nominalSize)
                }
            } else {
                // mod for every SPAN_FULL / SPAN_COURSE_ITEM_LARGE items
                val itemsPerRow = AdapterHomeContent.SPAN_FULL / AdapterHomeContent.SPAN_COURSE_ITEM_LARGE
                val posAdjusted = posRelative - AdapterHomeContent.HIGHLIGHT_COURSE_END_INDEX - 1
                if(posAdjusted % itemsPerRow == 0) {
                    // the first item of the row
                    itemView.adjustPadding(left = gutterSize * 3, right = nominalSize)
                } else if(posAdjusted % itemsPerRow == itemsPerRow - 1) {
                    // last item of row
                    itemView.adjustPadding(right = gutterSize * 3, left = nominalSize)
                } else {
                    // default padding
                    itemView.adjustPadding(left = nominalSize, right = nominalSize)
                }
            }
        }
    }

    ///// UTIL METHODS /////

    /**
     * Sets the gutter of this item.
     */
    private fun setGutter(left: Boolean = false, right: Boolean = false, size: Int) {
        if(left)
            itemView.adjustPadding(left = size)
        if(right)
            itemView.adjustPadding(right = size)
    }
}