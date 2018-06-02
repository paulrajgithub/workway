package com.asdev.edu.dialogs

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.R
import com.asdev.edu.adapters.AdapterCollectionsChoosable

object DialogChooseCollection {

    fun build(context: Context, callback: (Int) -> Unit): MaterialDialog.Builder {
        return MaterialDialog.Builder(context).apply {
            title(R.string.title_choose_collection)
            adapter(AdapterCollectionsChoosable(context, callback), LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
            negativeText(R.string.dialog_cancel)
        }

    }

}