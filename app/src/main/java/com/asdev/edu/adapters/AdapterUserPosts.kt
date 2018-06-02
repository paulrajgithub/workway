package com.asdev.edu.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.edu.R
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SharedData
import com.asdev.edu.views.VHPost
import io.reactivex.subjects.BehaviorSubject

class AdapterUserPosts(private val appContext: Context, private val actionHandler: BehaviorSubject<DUIAction<DPost>>) : RecyclerView.Adapter<VHPost>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHPost {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_post_home, parent, false)
        return VHPost(layout, actionHandler).apply { randomizeGradient() }
    }

    override fun getItemCount() = SharedData.duserRo(appContext)?.user?.posts?.size?: 0

    override fun onBindViewHolder(holder: VHPost, position: Int) {
        val posts = SharedData.duserRo(appContext)?.user?.posts?: return // TODO: graceful null handling

        holder.setToPost(posts[position])
    }

}