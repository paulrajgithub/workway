package com.asdev.edu.models

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.asdev.edu.MD_PRIMARY_COLOR_REFS
import com.asdev.edu.R

/**
 * An enumeration that represents a course that may be extended.
 */
enum class DCourse(
        @StringRes
        val titleRes: Int?,
        val title: String?,
        @DrawableRes
        val iconRes: Int,
        val parent: DCourse? = null,
        @ColorRes
        val colorRes: Int = MD_PRIMARY_COLOR_REFS[0]) {


    BIOLOGY(R.string.title_course_biology, null, R.drawable.ic_course_biology, colorRes = MD_PRIMARY_COLOR_REFS[0]),

    BUSINESS(R.string.title_course_business, null, R.drawable.ic_course_business, colorRes = MD_PRIMARY_COLOR_REFS[1]),
        MARKETING(R.string.title_course_marketing, null, R.drawable.ic_course_business, BUSINESS, colorRes = MD_PRIMARY_COLOR_REFS[2]),
        ACCOUNTING(R.string.title_course_accounting, null, R.drawable.ic_course_business, BUSINESS, colorRes = MD_PRIMARY_COLOR_REFS[3]),

    CHEMISTRY(R.string.title_course_chemistry, null, R.drawable.ic_course_chemistry, colorRes = MD_PRIMARY_COLOR_REFS[4]),
        ORGANIC_CHEMISTRY(R.string.title_course_organic_chem, null, R.drawable.ic_course_chemistry, CHEMISTRY, colorRes = MD_PRIMARY_COLOR_REFS[5]),

    ENGLISH(R.string.title_course_english, null, R.drawable.ic_course_english, colorRes = MD_PRIMARY_COLOR_REFS[0]),
        CREATIVE_WRITING(R.string.title_course_creative_writing, null, R.drawable.ic_course_english, ENGLISH, colorRes = MD_PRIMARY_COLOR_REFS[1]),

    LAW(R.string.title_course_law, null, R.drawable.ic_course_law_alt, colorRes = MD_PRIMARY_COLOR_REFS[2]),
        BUSINESS_LAW(R.string.title_course_business_law, null, R.drawable.ic_course_law_alt, LAW, colorRes = MD_PRIMARY_COLOR_REFS[3]),
        CRIMINAL_LAW(R.string.title_course_crim_law, null, R.drawable.ic_course_law_alt, LAW, colorRes = MD_PRIMARY_COLOR_REFS[4]),

    MATH(R.string.title_course_math, null, R.drawable.ic_course_math, colorRes = MD_PRIMARY_COLOR_REFS[5]),
        CALCULUS(R.string.title_course_calculus, null, R.drawable.ic_course_math, MATH, colorRes = MD_PRIMARY_COLOR_REFS[0]),
        ALGEBRA(R.string.title_course_algebra, null, R.drawable.ic_course_math, MATH, colorRes = MD_PRIMARY_COLOR_REFS[1]),
        FUNCTIONS(R.string.title_course_functions, null, R.drawable.ic_course_math, MATH, colorRes = MD_PRIMARY_COLOR_REFS[2]),

    PHYSICS(R.string.title_course_physics, null, R.drawable.ic_course_physics, colorRes = MD_PRIMARY_COLOR_REFS[3]);

    fun getUITitle(context: Context) =
            title ?:  // use title if not null
                if(titleRes != null) // otherwise use title res
                    context.resources.getString(titleRes)!!
                else
                    throw IllegalArgumentException("Both title and titleRes are null for course")
}