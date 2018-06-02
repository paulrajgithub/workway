package com.asdev.edu.services

import android.content.Context
import android.support.annotation.StringRes
import com.asdev.edu.R
import com.asdev.edu.models.ErrorCodes
import com.asdev.edu.models.Response
import java.util.*
import kotlin.math.absoluteValue

/**
 * A singleton which provides various localized message @StringRes codes.
 */
object Localization {

    /**
     * Returns a string res which describes the given response, or null if
     * it is unknown.
     */
    @StringRes
    fun getResponseMsg(response: Response): Int {
        if (response.code == Response.USER_NOT_FOUND.code) {
            return R.string.resp_error_user_not_found
        } else if (response.code == Response.POST_NOT_FOUND.code) {
            return R.string.resp_error_post_not_found
        } else if (response.code == Response.COLLECTION_NOT_FOUND.code) {
            return R.string.resp_error_collection_not_found
        }

        // bad request or something
        return when (response.desc) {
        //// General Errors ////
            ErrorCodes.NO_ERROR -> R.string.resp_error_none
            ErrorCodes.INVALID_JSON -> R.string.resp_error_internal

        //// User Errors ////
            ErrorCodes.USER_REG_ANON_TOKEN -> R.string.resp_error_reg_anon
            ErrorCodes.USER_REG_ALREADY_EXISTS -> R.string.resp_error_reg_exists
            ErrorCodes.USER_INVALID_ID -> R.string.resp_error_internal // not a user presentable error
            ErrorCodes.USER_PP_NO_REF -> R.string.resp_error_no_pp_ref
            ErrorCodes.USER_PP_INVALID_IMG -> R.string.resp_error_invalid_pp
            ErrorCodes.USER_REG_MISSING_REQ_PROP -> R.string.resp_error_internal // not presentable
            ErrorCodes.USER_REG_INVALID_GRADE -> R.string.resp_error_invalid_grade
            ErrorCodes.USER_GRADE_INVALID -> R.string.resp_error_invalid_grade
            ErrorCodes.USER_GRADE_NO_REF -> R.string.resp_error_no_grade
            ErrorCodes.USER_COURSES_NONE -> R.string.resp_error_no_courses
            ErrorCodes.USER_COLL_NO_NAME -> R.string.resp_error_no_coll_name
            ErrorCodes.USER_COLL_NO_REFS -> R.string.resp_error_no_refs
            ErrorCodes.USER_COLL_NAME_LEN -> R.string.resp_error_coll_name_len
            ErrorCodes.USER_COLL_POST_NUM -> R.string.resp_error_coll_post_num
            ErrorCodes.USER_REG_SCHOOL_NAME_LEN -> R.string.resp_error_school_name_len
            ErrorCodes.USER_REG_PLACE_ID_LEN -> R.string.resp_error_internal // not presentable
            ErrorCodes.USER_COLL_NO_PARAMS -> R.string.resp_error_internal // not presentable

        //// Post Errors ////
            ErrorCodes.POST_VIEW_FORBIDDEN -> R.string.resp_error_post_view_forb
            ErrorCodes.POST_INVALID_ID -> R.string.resp_error_invalid_post
            ErrorCodes.POST_CREATE_MISSING_PROPS -> R.string.resp_error_post_create_miss_props
            ErrorCodes.POST_CREATE_BAD_TITLE -> R.string.resp_error_post_bad_title
            ErrorCodes.POST_CREATE_BAD_REF -> R.string.resp_error_post_bad_ref
            ErrorCodes.POST_CREATE_TAGS_NUM -> R.string.resp_error_tag_num
            ErrorCodes.POST_CREATE_MISSING_REQ_TAGS -> R.string.resp_error_tags_req
            ErrorCodes.POST_REMOVE_NOT_OWNER -> R.string.resp_error_remove_owner

        //// Auth Errors ////
            ErrorCodes.AUTH_BAD_TOKEN -> R.string.resp_error_bad_token
            ErrorCodes.AUTH_NO_TOKEN -> R.string.resp_error_no_token

            else -> R.string.resp_error_internal
        }
    }

    /**
     * Converts a given time long (in milliseconds, since epoch) to a localized time string.
     */
    fun convertToTimeString(submitTime: Long, context: Context): String {
        // formats as so:
        // Recently
        // A few hours ago
        // A day ago
        // 2 days ago
        // 3 days ago
        // A week ago
        // 2 weeks ago
        // 3 weeks ago
        // A month ago
        // 2 months ago
        // A year ago
        // 2 years ago

        // create a calendar instance for easy management
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = submitTime
        val current = Calendar.getInstance()

        val result = StringBuilder()

        // go by years, month, weeks, days, hours, seconds
        val yearDiff = (current[Calendar.YEAR] - calendar[Calendar.YEAR]).absoluteValue
        if(yearDiff == 0) {
            val monthDiff = (current[Calendar.MONTH] - calendar[Calendar.MONTH]).absoluteValue
            if(monthDiff == 0) {
                val weekDiff = (current[Calendar.WEEK_OF_MONTH] - calendar[Calendar.WEEK_OF_MONTH]).absoluteValue
                if(weekDiff == 0) {
                    val dayDiff = (current[Calendar.DAY_OF_MONTH] - calendar[Calendar.DAY_OF_MONTH]).absoluteValue
                    if(dayDiff == 0) {
                        val hourDifferential = (current[Calendar.HOUR_OF_DAY] - calendar[Calendar.HOUR_OF_DAY]).absoluteValue
                        if(hourDifferential <= 3) {
                            // Recently
                            return context.getString(R.string.time_recently)
                        } else if(hourDifferential <= 12) {
                            // A few hours ago
                            return context.getString(R.string.time_few_hours_ago)
                        } else {
                            // Today
                            return context.getString(R.string.time_today)
                        }
                    } else {
                        // X days ago
                        if(dayDiff == 1) {
                            result.append(R.string.time_singular, context)
                            result.append(" ")
                            result.append(R.string.time_day_singular, context)
                        } else {
                            result.append(dayDiff)
                            result.append(" ")
                            result.append(R.string.time_day_plural, context)
                        }
                    }
                } else {
                    // X weeks ago
                    if(weekDiff == 1) {
                        result.append(R.string.time_singular, context)
                        result.append(" ")
                        result.append(R.string.time_week_singular, context)
                    } else {
                        result.append(weekDiff)
                        result.append(" ")
                        result.append(R.string.time_week_plural, context)
                    }
                }
            } else {
                // X months ago
                if(monthDiff == 1) {
                    result.append(R.string.time_singular, context)
                    result.append(" ")
                    result.append(R.string.time_month_singular, context)
                } else {
                    result.append(monthDiff)
                    result.append(" ")
                    result.append(R.string.time_month_plural, context)
                }
            }
        } else {
            // X years ago
            if(yearDiff == 1) {
                result.append(R.string.time_singular, context)
                result.append(" ")
                result.append(R.string.time_year_singular, context)
            } else {
                result.append(yearDiff)
                result.append(" ")
                result.append(R.string.time_year_plural, context)
            }
        }

        // append the last fragment; "ago"
        result.append(" ")
        result.append(R.string.time_ago, context)

        return result.toString()
    }
}

/**
 * Appends the given string resource id to this StringBuilder.
 */
fun StringBuilder.append(@StringRes res: Int, context: Context) {
    append(context.getString(res))
}