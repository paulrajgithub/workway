package com.asdev.edu.views

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.GRADIENTS
import com.asdev.edu.R
import com.asdev.edu.models.DCollection
import com.asdev.edu.models.DUIAction
import com.asdev.edu.services.Localization
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.absoluteValue

/**
 * A ViewHolder for a collection item.
 */
class VHCollection(itemView: View,
                   /**
                    * The collection this item is associated with.
                    */
                   var collection: DCollection?,
                   /**
                    * The action handler to post UI events to.
                    */
                   var actionHandler: BehaviorSubject<DUIAction<DCollection>>
                    ): RecyclerView.ViewHolder(itemView) {

    // ui elements fields
    private val title = itemView.findViewById<TextView>(R.id.collection_title)
    private val subtitle = itemView.findViewById<TextView>(R.id.collection_subtitle)
    // private val actionLike = itemView.findViewById<Button>(R.id.collection_like)
    private val actionEdit = itemView.findViewById<ImageButton>(R.id.collection_edit)
    private val actionShare = itemView.findViewById<ImageButton>(R.id.collection_share)
    private val gradient = itemView.findViewById<ImageView>(R.id.collection_gradient)
    private val time = itemView.findViewById<TextView>(R.id.collection_time)
    private val card = itemView.findViewById<CardView>(R.id.collection_card)

    init {
        collection?.let {
            setToCollection(it)
        }
    }

    fun setToCollection(collection: DCollection) {
        // change internal var
        this.collection = collection

        // assign gradient based on hashcode of UUID to make it consistent
        val index = collection.uuid.hashCode().absoluteValue % GRADIENTS.size
        gradient.setImageResource(GRADIENTS[index])

        // update UI elements
        title.text = collection.name
        subtitle.text = collection.creator?.name?: "Unknown"
        time.text = Localization.convertToTimeString(collection.submitTime, time.context)

        // rebind actions to actions with the
//        actionLike.setOnClickListener {
//            actionHandler.onNext(DUIAction(DUIAction.TYPE_COLLECTION_LIKE, collection.uuid))
//        }

        card.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_COLLECTION_FULLSCREEN, collection))
        }

        actionEdit.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_COLLECTION_EDIT, collection))
        }

        actionShare.setOnClickListener{
            actionHandler.onNext(DUIAction(DUIAction.TYPE_COLLECTION_SHARE, collection))
        }
    }

}

/**
 * Holds a blank (a plain view) view
 */
class VHBlank(view: View): RecyclerView.ViewHolder(view)