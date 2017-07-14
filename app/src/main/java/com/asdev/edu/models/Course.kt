package com.asdev.edu.models

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.asdev.edu.MD_COURSE_COLORS
import com.asdev.edu.R
import com.asdev.edu.containsBits

/**
 * An enumeration that represents a course that may be extended.
 */
enum class DCourse(
        /**
         * The string resource id for the title.
         */
        @StringRes
        val titleRes: Int?,
        /**
         * The title string itself. Will be preferred over the titleRes.
         */
        val title: String?,
        /**
         * The drawable reference for the icon of this course.
         */
        @DrawableRes
        val iconRes: Int,
        /**
         * An optional parent of this course. Use for sub-course branching.
         */
        val parent: DCourse? = null,
        /**
         * The background color reference for this course.
         */
        @ColorRes
        val colorRes: Int = MD_COURSE_COLORS[0]) {

    BIOLOGY(R.string.title_course_biology, null, R.drawable.ic_course_biology, colorRes = MD_COURSE_COLORS[0]),

    BUSINESS(R.string.title_course_business, null, R.drawable.ic_course_business_alt, colorRes = MD_COURSE_COLORS[1]),
        MARKETING(R.string.title_course_marketing, null, R.drawable.ic_course_marketing_alt, BUSINESS, colorRes = MD_COURSE_COLORS[2]),
        ACCOUNTING(R.string.title_course_accounting, null, R.drawable.ic_course_accounting, BUSINESS, colorRes = MD_COURSE_COLORS[3]),

    CHEMISTRY(R.string.title_course_chemistry, null, R.drawable.ic_course_chemistry, colorRes = MD_COURSE_COLORS[4]),
        ORGANIC_CHEMISTRY(R.string.title_course_organic_chem, null, R.drawable.ic_course_chemistry, CHEMISTRY, colorRes = MD_COURSE_COLORS[5]),

    ENGLISH(R.string.title_course_english, null, R.drawable.ic_course_english, colorRes = MD_COURSE_COLORS[0]),
        CREATIVE_WRITING(R.string.title_course_creative_writing, null, R.drawable.ic_course_creative_writing, ENGLISH, colorRes = MD_COURSE_COLORS[1]),

    LAW(R.string.title_course_law, null, R.drawable.ic_course_law_alt, colorRes = MD_COURSE_COLORS[2]),
        BUSINESS_LAW(R.string.title_course_business_law, null, R.drawable.ic_course_business_law, LAW, colorRes = MD_COURSE_COLORS[3]),
        CRIMINAL_LAW(R.string.title_course_crim_law, null, R.drawable.ic_course_law, LAW, colorRes = MD_COURSE_COLORS[4]),

    MATH(R.string.title_course_math, null, R.drawable.ic_course_math_alt, colorRes = MD_COURSE_COLORS[5]),
        CALCULUS(R.string.title_course_calculus, null, R.drawable.ic_course_calculus, MATH, colorRes = MD_COURSE_COLORS[0]),
        ALGEBRA(R.string.title_course_algebra, null, R.drawable.ic_course_math, MATH, colorRes = MD_COURSE_COLORS[1]),
        FUNCTIONS(R.string.title_course_functions, null, R.drawable.ic_course_functions, MATH, colorRes = MD_COURSE_COLORS[2]),

    PHYSICS(R.string.title_course_physics, null, R.drawable.ic_course_physics, colorRes = MD_COURSE_COLORS[3]);

    companion object {

        /**
         * Returns the tag as a course object, or null if it is not.
         */
        fun fromTag(tag: DTag?): DCourse? {
            if(tag == null || tag.id !is String || !(tag.scope containsBits TAG_SCOPE_COURSE)) {
                return null
            }

            return valueOf(tag.id)
        }

    }

    /**
     * Returns all of the sub courses of this course.
     */
    fun subCourses(): List<DCourse> =
            values().filter { it.parent == this } // take all where the parent is this

    /**
     * Resolves the title contained within either the title string or titleRes resource.
     */
    fun resolveTitle(context: Context): String =
            title ?:  // use title if not null
                if(titleRes != null) // otherwise use title res
                    context.resources.getString(titleRes)!!
                else
                    throw IllegalArgumentException("Both title and titleRes are null for course")

    /**
     * Returns this course object as a matching DTag object.
     */
    fun toTag(context: Context) =
            DTag(text = resolveTitle(context), id = name, scope = TAG_SCOPE_COURSE)
}