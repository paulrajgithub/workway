package com.asdev.edu.models

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
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
        val parent: DCourse? = null) {


    BIOLOGY(R.string.title_course_biology, null, R.drawable.ic_course_biology),

    BUSINESS(R.string.title_course_business, null, R.drawable.ic_course_business),
        MARKETING(R.string.title_course_marketing, null, R.drawable.ic_course_business, BUSINESS),
        ACCOUNTING(R.string.title_course_accounting, null, R.drawable.ic_course_business, BUSINESS),

    CHEMISTRY(R.string.title_course_chemistry, null, R.drawable.ic_course_chemistry),
        ORGANIC_CHEMISTRY(R.string.title_course_organic_chem, null, R.drawable.ic_course_chemistry, CHEMISTRY),

    ENGLISH(R.string.title_course_english, null, R.drawable.ic_course_english),
        CREATIVE_WRITING(R.string.title_course_creative_writing, null, R.drawable.ic_course_english, ENGLISH),

    LAW(R.string.title_course_law, null, R.drawable.ic_course_law_alt),
        BUSINESS_LAW(R.string.title_course_business_law, null, R.drawable.ic_course_law_alt, LAW),
        CRIMINAL_LAW(R.string.title_course_crim_law, null, R.drawable.ic_course_law_alt, LAW),

    MATH(R.string.title_course_math, null, R.drawable.ic_course_math),
        CALCULUS(R.string.title_course_calculus, null, R.drawable.ic_course_math, MATH),
        ALGEBRA(R.string.title_course_algebra, null, R.drawable.ic_course_math, MATH),
        FUNCTIONS(R.string.title_course_functions, null, R.drawable.ic_course_math, MATH),

    PHYSICS(R.string.title_course_physics, null, R.drawable.ic_course_physics);

    fun getUITitle(context: Context) =
            title ?:  // use title if not null
                if(titleRes != null) // otherwise use title res
                    context.resources.getString(titleRes)
                else
                    throw IllegalArgumentException("Both title and titleRes are null for course")
}