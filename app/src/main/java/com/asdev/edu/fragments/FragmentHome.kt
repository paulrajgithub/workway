package com.asdev.edu.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asdev.edu.R

class FragmentHome: Fragment() {

    private val adapter = AdapterAutoImport()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recycler = view.findViewById(R.id.home_recycler_auto_import) as RecyclerView
        // set layout to linear
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // set adapter
        recycler.adapter = adapter

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }

}

class AdapterAutoImport: RecyclerView.Adapter<VHAutoImport>() {

    override fun getItemCount() = 5

    override fun onBindViewHolder(holder: VHAutoImport?, position: Int) {
        // TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VHAutoImport {
        // inflate a new view
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_auto_import, parent, false)
        // create a holder
        val holder = VHAutoImport(view)
        return holder
    }

}

class VHAutoImport(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val imgView = itemView.findViewById(R.id.image) as ImageView

    fun bind() {
        // TODO
    }
}