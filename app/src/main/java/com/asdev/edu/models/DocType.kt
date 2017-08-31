package com.asdev.edu.models

import android.content.Context
import android.support.annotation.StringRes
import com.asdev.edu.R
import com.asdev.edu.containsBits

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
        fun fromTag(tag: DTag?): DDocType? {
            // if tag is null, return null, or if isn't string, or if tag scope isnt even a type
            if (tag == null || tag.id !is String || !(tag.scope containsBits TAG_SCOPE_TYPE)) {
                return null
            }

            // the tag id is the name of the course
            return valueOf(tag.id)
        }
    }

    /**
     * Resolves the title of this doc type using the given context.
     */
    fun resolveTitle(context: Context): String =
                    context.resources.getString(titleRef)!!

    /**
     * Converts this DocType to a tag object.
     */
    fun toTag(context: Context)
        = DTag(context.getString(titleRef), name, TAG_SCOPE_TYPE)
}