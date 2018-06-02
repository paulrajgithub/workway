package com.asdev.edu.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import com.asdev.edu.views.VHPost
import io.reactivex.subjects.BehaviorSubject

class AdapterPosts(private var posts: MutableList<DPost>, private val behaviorSubject: BehaviorSubject<DUIAction<DPost>>): RecyclerView.Adapter<VHPost>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHPost {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_home, parent, false)
        return VHPost(view, behaviorSubject).apply { randomizeGradient() }
    }

    fun setItems(posts: MutableList<DPost>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: VHPost, position: Int) {
        holder.setToPost(posts[position])
    }

}