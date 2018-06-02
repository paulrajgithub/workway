package com.asdev.edu.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.GRADIENTS
import com.asdev.edu.R
import com.asdev.edu.RANDOM
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SharedData
import com.asdev.edu.services.Localization
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.reactivex.subjects.BehaviorSubject

class VHPost(itemView: View, private val behaviorSubject: BehaviorSubject<DUIAction<DPost>>): RecyclerView.ViewHolder(itemView) {

    private val title = itemView.findViewById<TextView>(R.id.post_title)
    private val body = itemView.findViewById<TextView>(R.id.post_body)
    private val img = itemView.findViewById<ImageView>(R.id.post_img)
    private val likes = itemView.findViewById<Button>(R.id.post_like)
    private val send = itemView.findViewById<ImageButton>(R.id.post_send)
    private val save = itemView.findViewById<ImageButton>(R.id.post_save)

    fun randomizeGradient() {
        Glide.with(img.context).clear(img)

        img.setImageResource(GRADIENTS[RANDOM.nextInt(GRADIENTS.size)])
    }

    fun setToPost(post: DPost) {
        likes.isEnabled = true
        title.text = post.title
        // body.text = post.owner?.name?: body.context.getString(R.string.text_person_unknown) // use unknown or date string?
        body.text = post.owner?.name?: Localization.convertToTimeString(post.submitTime, itemView.context)

        likes.text = post.numLikes.toString()

        // check if filled in
        if(post.likes.contains(SharedData.duserRo(itemView.context)?.user?._id?: "")) {
            likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp, 0, 0, 0)
            // disable button
            likes.isEnabled = false
        } else {
            likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0)
        }

        likes.setOnClickListener {
            val user = SharedData.duserRo(itemView.context)!!
            // make sure we're not liking are on post
            if(post.ownerId == user.user._id) {
                // emit a post like action to the handler, but don't update self state
                behaviorSubject.onNext(DUIAction(DUIAction.TYPE_POST_LIKE, post))

                return@setOnClickListener
            }

            // emit a post like action
            behaviorSubject.onNext(DUIAction(DUIAction.TYPE_POST_LIKE, post))
            // show as liked button
            likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp, 0, 0, 0)
            // disable the button
            likes.isEnabled = false
            // increment likes count
            likes.text = (post.numLikes + 1).toString()
        }

        send.setOnClickListener {
            behaviorSubject.onNext(DUIAction(DUIAction.TYPE_POST_SEND, post))
        }

        save.setOnClickListener {
            behaviorSubject.onNext(DUIAction(DUIAction.TYPE_POST_SAVE, post))
        }

        itemView.setOnClickListener {
            behaviorSubject.onNext(DUIAction(DUIAction.TYPE_POST_FULLSCREEN, post))
        }

        // do a glide fetch into the img view
        Glide.with(img.context)
                .load(post.ref)
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(img.drawable)
                )
                .transition(DrawableTransitionOptions().crossFade(200))
                .into(img)
    }
}