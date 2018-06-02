/*
 *  NOTICE FOR FILE:
 *  ErrorResponse.kt
 *
 *  Copyright (c) 2015-2017. Created by and property of Asdev Software Development, on 7/16/17 1:35 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module edu-backend_main
 */

package com.asdev.edu.models

class ResponseException(val response: Response): Exception("An server occurred, with the response $response")

data class Response(
        val error: Boolean = true,
        val code: Int,
        val httpCode: Int,
        val desc: Int = ErrorCodes.NO_ERROR) {

    companion object {

        ///// 200 series codes /////
        val OK = Response(false, 200, 200)

        ///// 400 series codes /////
        val BAD_REQUEST = Response(true, 400, 400)
        val UNAUTHORIZED = Response(true, 401, 401)
        val FORBIDDEN = Response(true, 403, 403)
        val POST_NOT_FOUND = Response(true, 404, 404)
        val USER_NOT_FOUND = Response(true, 405, 404)
        val COLLECTION_NOT_FOUND = Response(true, 406, 404)

        ///// 500 series codes /////
        val SERVER_ERROR = Response(true, 500, 500)
    }

    fun desc(desc: Int) = Response(error, code, httpCode, desc)
}

class ErrorCodes {

    companion object {


        ///// General error codes - prefix of 000 /////
        val NO_ERROR = 0
        val INVALID_JSON = /*00*/ 1

        ///// User error codes - prefix of 100 /////
        val USER_REG_ANON_TOKEN = 101
        val USER_REG_ALREADY_EXISTS = 102
        val USER_INVALID_ID = 103
        val USER_PP_NO_REF = 104
        val USER_PP_INVALID_IMG = 105
        val USER_REG_MISSING_REQ_PROP = 106
        val USER_REG_INVALID_GRADE = 107
        val USER_GRADE_NO_REF = 108
        val USER_GRADE_INVALID = 109
        val USER_COURSES_NONE = 110
        val USER_COLL_NO_NAME = 111
        val USER_COLL_NO_REFS = 112
        val USER_COLL_NAME_LEN = 113
        val USER_COLL_POST_NUM = 114
        val USER_REG_SCHOOL_NAME_LEN = 115
        val USER_REG_PLACE_ID_LEN = 116
        val USER_COLL_NO_PARAMS = 117

        ///// Post error codes - prefix of 200 ////
        val POST_VIEW_FORBIDDEN = 201
        val POST_INVALID_ID = 202
        val POST_CREATE_MISSING_PROPS = 203
        val POST_CREATE_BAD_TITLE = 204
        val POST_CREATE_BAD_REF = 206
        val POST_CREATE_TAGS_NUM = 207
        val POST_CREATE_MISSING_REQ_TAGS = 208
        val POST_REMOVE_NOT_OWNER = 209

        ///// Auth error codes - prefix of 300 ///
        val AUTH_BAD_TOKEN = 301
        val AUTH_NO_TOKEN = 302

    }

}