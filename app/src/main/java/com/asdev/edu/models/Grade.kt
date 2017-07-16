package com.asdev.edu.models

import android.content.Context
import android.support.annotation.StringRes
import com.asdev.edu.R
import com.asdev.edu.containsBits

/**
 * An enumeration that represents the grade in which a user is in.
 * In order of smallest to largest.
 */
enum class DGrade(
        /**
         * The resource id of the title of this grade.
         */
        @StringRes
        val titleRes: Int,
        /**
         * The title of this grade.
         */
        val title: String? = null) {

    GRADE_9(R.string.title_grade_gr9),
    GRADE_10(R.string.title_grade_gr10),
    GRADE_11(R.string.title_grade_gr11),
    GRADE_12(R.string.title_grade_gr12),

    UNI_1ST(R.string.title_grade_uni1),
    UNI_2ND(R.string.title_grade_uni2),
    UNI_3RD(R.string.title_grade_uni3),
    UNI_4TH(R.string.title_grade_uni4),
    UNI_5TH(R.string.title_grade_uni5);

    companion object {

        /**
         * Returns the tag as a grade object, or null if it is not.
         */
        fun fromTag(tag: DTag?): DGrade? {
            if(tag == null || tag.id !is String || !(tag.scope containsBits TAG_SCOPE_GRADE))
                return null

            return byName(tag.id)
        }

        /**
         * Returns the associated enum, or null if it is an invalid name.
         */
        fun byName(name: String): DGrade? {
            try {
                return valueOf(name)
            } catch (e: Exception) {
                return null
            }
        }
    }

    /**
     * Resolves the title contained within either the title string or titleRes resource.
     */
    fun resolveTitle(context: Context): String =
            title ?: context.resources.getString(titleRes)!!

    fun toTag(context: Context)
        = DTag(resolveTitle(context), name, TAG_SCOPE_GRADE)

}