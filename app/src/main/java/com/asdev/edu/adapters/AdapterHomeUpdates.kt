package com.asdev.edu.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DUIAction
import com.asdev.edu.views.VHUpdateItem
import io.reactivex.subjects.BehaviorSubject

/**
 * Represents the id of a post.
 */
typealias PostId = Int

/**
 * A class that supplies update items to the home fragment.
 */
class AdapterHomeUpdates(val actionHandler: BehaviorSubject<DUIAction<PostId>>): RecyclerView.Adapter<VHUpdateItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VHUpdateItem {
        // inflate the layout item
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(R.layout.item_update, parent, false)
        // construct a new empty VH
        return VHUpdateItem(view, null, actionHandler)
    }

    override fun onBindViewHolder(holder: VHUpdateItem?, position: Int) {
        // set to the post item
        // TODO: bind to item
    }

    override fun getItemCount() = 6
}