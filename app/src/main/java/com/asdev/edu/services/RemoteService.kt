package com.asdev.edu.services

import com.androidnetworking.error.ANError
import com.asdev.edu.GSON
import com.asdev.edu.models.*
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.fabric.sdk.android.services.network.HttpRequest.CONTENT_TYPE_JSON
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONArray
import org.json.JSONObject

const val REMOTE_BASE_URL = "http://192.168.2.250/api"

const val REMOTE_POST_VIEW = REMOTE_BASE_URL + "/posts/view/"
const val REMOTE_POST_CREATE = REMOTE_BASE_URL + "/posts/create"
const val REMOTE_POST_UPDATE = REMOTE_BASE_URL + "/posts/update/"
const val REMOTE_POST_REMOVE = REMOTE_BASE_URL + "/posts/remove/"

const val REMOTE_USERS_VIEW = REMOTE_BASE_URL + "/users/view/"
const val REMOTE_USERS_PROFILE = REMOTE_BASE_URL + "/users/profile/"
const val REMOTE_USERS_REGISTER = REMOTE_BASE_URL + "/users/register"

const val REMOTE_UPDATE = REMOTE_BASE_URL + "/users/update"
const val REMOTE_UPDATE_FOLLOW = REMOTE_UPDATE + "/follow/"
const val REMOTE_UPDATE_UNFOLLOW = REMOTE_UPDATE + "/unfollow/"
const val REMOTE_UPDATE_PROFILE_PHOTO = REMOTE_UPDATE + "/profile_photo"
const val REMOTE_UPDATE_STARRED_COURSES = REMOTE_UPDATE + "/starred_courses"
const val REMOTE_UPDATE_GRADE = REMOTE_UPDATE + "/grade"

const val REMOTE_FEED = REMOTE_BASE_URL + "/feed"

const val REMOTE_KEY_AUTH = "auth"
const val REMOTE_KEY_START = "start"
const val REMOTE_KEY_LENGTH = "length"

object RemoteService {

    const val KEY_PP_REF = "profilePicRef"
    const val KEY_STARRED_COURSES = "starredCourses"
    const val KEY_GRADE = "grade"
    const val KEY_SCHOOL_NAME = "schoolName"
    const val KEY_SCHOOL_PLACE_ID = "schoolPlaceId"

    /**
     * Registers a new user on the remote service with the given properties.
     * Returns an observable, which on completion, will return a RemoteResponse sent
     * by the server.
     */
    @JvmStatic
    fun userRegister(authToken: String, profilePicRef: String?, starredCourses: List<DCourse>?, grade: DGrade, school: DSchool): Observable<RemoteResponse<DUser>> {
        return Observable.create<RemoteResponse<DUser>> {
            emitter ->

            // build a json object with the req props
            val json = JSONObject()
            if (profilePicRef != null) {
                json.put(KEY_PP_REF, profilePicRef)
            }
            if (starredCourses != null) {
                json.put(KEY_STARRED_COURSES, JSONArray(starredCourses))
            }
            json.put(KEY_GRADE, grade)
            json.put(KEY_SCHOOL_NAME, school.name)
            json.put(KEY_SCHOOL_PLACE_ID, school.placeId)

            // now we have our post content, build our actual request
            val subscription = Rx2AndroidNetworking.post(REMOTE_USERS_REGISTER)
                    .addJSONObjectBody(json) // add json body
                    .setContentType(CONTENT_TYPE_JSON) // set to application/json content type
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = {
                                resp ->
                                // pass the error as the object
                                try {
                                    val exception = resp as ANError // if a network error, try and parse it
                                    val error = GSON.fromJson(exception.errorBody, Response::class.java)
                                    emitter.onNext(RemoteResponse(null, error))
                                    emitter.onComplete()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    emitter.onError(e)
                                }
                            },
                            onComplete = {
                                // we have already completed the request
                                // no need to do anything here
                            },
                            onNext = {
                                // successful response, parse object
                                try {
                                    val user = GSON.fromJson(it.toString(0), DUser::class.java)
                                    emitter.onNext(RemoteResponse(user, null))
                                    emitter.onComplete()
                                } catch (e: Exception) {
                                    emitter.onError(e)
                                }
                            }
                    )

            // can cancel the request
            emitter.setDisposable(subscription)
        }
    }

}

data class RemoteResponse<out T>(val payload: T?, val error: Response?)