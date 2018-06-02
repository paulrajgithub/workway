package com.asdev.edu.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.R
import com.asdev.edu.models.DCollection

class VHCollectionChoosable(itemView: View): RecyclerView.ViewHolder(itemView) {

    val title = itemView.findViewById<TextView>(R.id.collection_title)
    val icon = itemView.findViewById<ImageView>(R.id.collection_icon)

    fun setToCollection(collection: DCollection) {
        title.text = collection.name
        icon.setImageDrawable(null)
    }
}