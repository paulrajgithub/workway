package com.asdev.edu.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DUIAction
import com.asdev.edu.views.VHUpdateItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.subjects.BehaviorSubject

/**
 * Represents the id of a post.
 */
typealias PostId = Int

/**
 * View type for the AdapterHomeContent update item.
 */
const val VT_NORMAL = 0
/**
 * View type for the AdapterHomeContent update item. Denotes
 * the item as being flipped.
 */
const val VT_FLIPPED = 1

// TODO: these are temporary
val SAMPLE_IMGS = arrayOf(
        "http://kuuv.io/i/WkWLSzG.jpg",
        "http://kuuv.io/i/pNrneD6.jpg",
        "http://kuuv.io/i/B7TRR3F.jpg",
        "http://kuuv.io/i/iDcNbJq.jpg",
        "http://kuuv.io/i/BrGqEUu.jpg",
        "http://kuuv.io/i/rtJo6WA.jpg",
        "http://kuuv.io/i/dAFnRAX.jpg",
        "http://kuuv.io/i/WkWLSzG.jpg",
        "http://kuuv.io/i/pNrneD6.jpg",
        "http://kuuv.io/i/B7TRR3F.jpg",
        "http://kuuv.io/i/iDcNbJq.jpg",
        "http://kuuv.io/i/BrGqEUu.jpg",
        "http://kuuv.io/i/rtJo6WA.jpg",
        "http://kuuv.io/i/WkWLSzG.jpg",
        "http://kuuv.io/i/pNrneD6.jpg",
        "http://kuuv.io/i/B7TRR3F.jpg",
        "http://kuuv.io/i/iDcNbJq.jpg",
        "http://kuuv.io/i/BrGqEUu.jpg",
        "http://kuuv.io/i/rtJo6WA.jpg",
        "http://kuuv.io/i/WkWLSzG.jpg",
        "http://kuuv.io/i/pNrneD6.jpg",
        "http://kuuv.io/i/B7TRR3F.jpg",
        "http://kuuv.io/i/iDcNbJq.jpg",
        "http://kuuv.io/i/BrGqEUu.jpg",
        "http://kuuv.io/i/rtJo6WA.jpg",
        "http://kuuv.io/i/WkWLSzG.jpg",
        "http://kuuv.io/i/pNrneD6.jpg",
        "http://kuuv.io/i/B7TRR3F.jpg",
        "http://kuuv.io/i/iDcNbJq.jpg",
        "http://kuuv.io/i/BrGqEUu.jpg",
        "http://kuuv.io/i/rtJo6WA.jpg"
)

val PP_PHOTOS = arrayOf(
        "http://kuuv.io/i/3oNAIVo.jpg",
        "http://kuuv.io/i/lMq9BLJ.jpg"
)

/**
 * A class that supplies update items to the home fragment.
 */
class AdapterHomeUpdates(val actionHandler: BehaviorSubject<DUIAction<String>>): RecyclerView.Adapter<VHUpdateItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VHUpdateItem {
        // inflate the layout item
        val inflater = LayoutInflater.from(parent?.context)
        // val view = inflater.inflate(if(viewType == VT_NORMAL) R.layout.item_update_alt else R.layout.item_update_alt_flipped, parent, false)
        val view = inflater.inflate(R.layout.item_update, parent, false)
        // construct a new empty VH
        return VHUpdateItem(view, null, actionHandler)
    }

    // for using the alt items
//    override fun getItemViewType(position: Int): Int {
//        return if(position % 2 == 0) VT_FLIPPED else VT_NORMAL
//    }

    override fun onBindViewHolder(holder: VHUpdateItem?, position: Int) {
        // set to the post item
        // TODO: bind to item
        val imgView = holder?.imgView ?: return
        val ppView = holder.ppView

        // use source disk caching as it will be loaded into a larger view
        // later if clicked upon
        Glide
                .with(imgView.context)
                .load(SAMPLE_IMGS[position])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_image_placeholder)
                .crossFade()
                .into(imgView)

        // dont animate as the circle image view doesn't like the animation drawable
        Glide
                .with(imgView.context)
                .load(PP_PHOTOS[position % PP_PHOTOS.size])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_person_default)
                .dontAnimate()
                .into(ppView)
    }

    override fun onViewRecycled(holder: VHUpdateItem?) {
        super.onViewRecycled(holder)

        val imgView = holder?.imgView?: return
        // release any resources on this view
        Glide.clear(imgView)
        Glide.clear(holder.ppView)
    }

    override fun getItemCount() = SAMPLE_IMGS.size
}