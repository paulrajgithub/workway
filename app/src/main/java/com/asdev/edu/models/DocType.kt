package com.asdev.edu.models

import android.content.Context
import android.support.annotation.StringRes
import com.asdev.edu.R

enum class DDocType(
        /**
         * The reference to the name of this doc type.
         */
        @StringRes
        val titleRef: Int) {

    TEXTBOOK(R.string.title_doc_textbook),

    HOMEWORK(R.string.title_doc_homework),

    NOTE(R.string.title_doc_note),

    LAB(R.string.title_doc_lab),

    ESSAY(R.string.title_doc_essay),

    REPORT(R.string.title_doc_report),

    ASSIGNMENT(R.string.title_doc_assignment);

    companion object {

        /**
         * Converts the given tag object to a DocType if applicable.
         */
        fun fromTag(tag: DTag?) =
                tag?.id as? DDocType
    }

    /**
     * Converts this DocType to a tag object.
     */
    fun toTag(context: Context)
        = DTag(context.getString(titleRef), this, TAG_SCOPE_TYPE)
}