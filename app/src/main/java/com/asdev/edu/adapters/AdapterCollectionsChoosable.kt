package com.asdev.edu.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.SharedData
import com.asdev.edu.views.VHCollectionChoosable

class AdapterCollectionsChoosable(context: Context, val callback: (Int) -> Unit): RecyclerView.Adapter<VHCollectionChoosable>() {

    private val items = SharedData.duserRo(context)?.user?.collections?.sortedByDescending { it.submitTime }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHCollectionChoosable {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_collection_selector, parent, false)
        return VHCollectionChoosable(layout)
    }

    override fun getItemCount() = (items?.size?: 0) + 2 // new collection, quick saved

    override fun onBindViewHolder(holder: VHCollectionChoosable, position: Int) {
        holder.itemView.setOnClickListener {
            callback(position)
        }

        if(position == 0) {
            // new collection
            holder.title.setText(R.string.text_new_collection)
            // set to add icon
            holder.icon.setImageResource(R.drawable.ic_add_black_24dp)

            return
        } else if(position == 1){
            // quick saved
            holder.title.setText(R.string.text_quick_save_collection)
            // set to star icon
            holder.icon.setImageResource(R.drawable.ic_star_black_24dp)

            return
        }

        if(items == null)
            return

        holder.setToCollection(items[position - 2])
    }


}