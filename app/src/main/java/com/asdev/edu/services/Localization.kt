package com.asdev.edu.services

import android.support.annotation.StringRes
import com.asdev.edu.R
import com.asdev.edu.models.ErrorCodes
import com.asdev.edu.models.Response

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

}