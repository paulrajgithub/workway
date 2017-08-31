package com.asdev.edu.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.asdev.edu.R
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import io.reactivex.subjects.BehaviorSubject

/**
 * A view holder for a standard update item, as defined by layout R.layout.item_update
 */
class VHUpdateItem(view: View,
                   /**
                    * The post associated with this ViewHolder, or null if none is.
                    */
                   var post: DPost?,
                   /**
                    * A handler to post UI events to.
                    */
                   val actionHandler: BehaviorSubject<DUIAction<String>>): RecyclerView.ViewHolder(view) {

    init {
        // call self set to post if one is provided
        post?.let {
            setToPost(it)
        }
    }

    val imgView = view.findViewById(R.id.update_image) as ImageView
    val ppView = view.findViewById(R.id.update_profile_photo) as ImageView
    val titleView = view.findViewById(R.id.update_title) as TextView
    val subtitleView = view.findViewById(R.id.update_subtitle) as TextView

    val actionSave = view.findViewById(R.id.update_action_save) as ImageButton
    val actionSend = view.findViewById(R.id.update_action_send) as ImageButton

    /**
     * Sets this update view to the given post.
     */
    fun setToPost(post: DPost) {
        this.post = post

        // rebind the button actions
        actionSave.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_SAVE, post._id!!))
        }

        actionSend.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_SEND, post._id!!))
        }

        // update the ui
        titleView.text = post.title

        // take relevant subtitle fields
        val course = post.resolveCourse()
        val professor = post.resolveProfessor()

        subtitleView.text = course?.resolveTitle(itemView.context)?: // try and resolve the course title
                professor?: // use professor if specified
                post.resolveDocType()?.resolveTitle(itemView.context)?: // try and use the doc type if given
                post.resolveSchool()?.name?: // try and use the school if given
                ""

        // TODO: glide fetch post ref
        post.ref
        // user should be already resolved
        val user = post.owner

        if (user != null) {
            // TODO: glide fetch user pp icon ref
            user.profilePicRef
            user
        } else {
            // TODO: user resolution
        }
    }
}