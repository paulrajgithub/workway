package com.asdev.edu.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.R
import kotlin.math.roundToInt

object DialogLike {

    fun build(context: Context, userName: String, postName: String, amount: Double): MaterialDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_like, null, false)

        view.findViewById<TextView>(R.id.dialog_like_top_greet).text = context.getString(R.string.text_user_liked_post, userName, postName)
        view.findViewById<TextView>(R.id.dialog_like_bottom_greet).text = context.getString(R.string.text_drt_sent, amount.roundToInt())

        return MaterialDialog.Builder(context)
                .customView(view, false)
                .build()
    }

}