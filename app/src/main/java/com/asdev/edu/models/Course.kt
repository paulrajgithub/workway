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
        val titleRes: Int,
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
        val parent: DCourse? = null) {

    BIOLOGY(R.string.title_course_biology, null, R.drawable.ic_course_biology),

    BUSINESS(R.string.title_course_business, null, R.drawable.ic_course_business_alt),
        MARKETING(R.string.title_course_marketing, null, R.drawable.ic_course_marketing_alt, BUSINESS),
        ACCOUNTING(R.string.title_course_accounting, null, R.drawable.ic_course_accounting, BUSINESS),

    CHEMISTRY(R.string.title_course_chemistry, null, R.drawable.ic_course_chemistry),
        ORGANIC_CHEMISTRY(R.string.title_course_organic_chem, null, R.drawable.ic_course_chemistry, CHEMISTRY),

    ENGLISH(R.string.title_course_english, null, R.drawable.ic_course_english),
        CREATIVE_WRITING(R.string.title_course_creative_writing, null, R.drawable.ic_course_creative_writing, ENGLISH),

    LAW(R.string.title_course_law, null, R.drawable.ic_course_law_alt),
        BUSINESS_LAW(R.string.title_course_business_law, null, R.drawable.ic_course_business_law, LAW),
        CRIMINAL_LAW(R.string.title_course_crim_law, null, R.drawable.ic_course_law, LAW),

    MATH(R.string.title_course_math, null, R.drawable.ic_course_math_alt),
        CALCULUS(R.string.title_course_calculus, null, R.drawable.ic_course_calculus, MATH),
        ALGEBRA(R.string.title_course_algebra, null, R.drawable.ic_course_math, MATH),
        FUNCTIONS(R.string.title_course_functions, null, R.drawable.ic_course_functions, MATH),

    PHYSICS(R.string.title_course_physics, null, R.drawable.ic_course_physics);

    /**
     * The background color reference for this course.
     */
    @ColorRes
    val colorRes: Int = MD_COURSE_COLORS[ordinal % MD_COURSE_COLORS.size]
    
    companion object {

        /**
         * Returns the tag as a course object, or null if it is not.
         */
        fun fromTag(tag: DTag?): DCourse? {
            if(tag == null || tag.id !is String || !(tag.scope containsBits TAG_SCOPE_COURSE)) {
                return null
            }

            return byName(tag.id)
        }

        /**
         * Returns the associated enum, or null if it is an invalid name.
         */
        fun byName(name: String): DCourse? {
            try {
                return valueOf(name)
            } catch (e: Exception) {
                return null
            }
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
                    context.resources.getString(titleRes)!! // use title res otherwise

    /**
     * Returns this course object as a matching DTag object.
     */
    fun toTag(context: Context) =
            DTag(text = resolveTitle(context), id = name, scope = TAG_SCOPE_COURSE)
}