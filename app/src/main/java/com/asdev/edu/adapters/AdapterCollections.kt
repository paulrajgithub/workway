package com.asdev.edu.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DCollection
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SharedData
import com.asdev.edu.views.VHBlank
import com.asdev.edu.views.VHCollection
import io.reactivex.subjects.BehaviorSubject

const val VT_NORMAL = 0
const val VT_HEADER = 2

class AdapterCollections(appContext: Context, private val actionHandler: BehaviorSubject<DUIAction<DCollection>>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = SharedData.duserRo(appContext)?.user?.collections?.sortedByDescending { it.submitTime }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)
            return VT_HEADER
        return VT_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view: View

        // check if header type
        if(viewType == VT_HEADER) {
            view = inflater.inflate(R.layout.header_collections, parent, false)
            return VHBlank(view)
        }

        view = inflater.inflate(R.layout.item_collection, parent, false)


        return VHCollection(view, null, actionHandler)
    }

    override fun getItemCount() = 1 + (items?.size?: 0)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == 0 || items == null) {
            // header
            return
        }

        if(holder !is VHCollection)
            return

        // call the internal bind method
        holder.setToCollection(items[position - 1])
    }

}