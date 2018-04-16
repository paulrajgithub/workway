package com.asdev.edu.services

import com.androidnetworking.error.ANError
import com.asdev.edu.GSON
import com.asdev.edu.models.*
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONArray
import org.json.JSONObject

/**
 * A singleton which acts as an API transport between the edu-backend server and the Android client.
 * All methods are concurrent and the concurrency is implemented using RxJava's [Observable]s.
 */
object RemoteService {

    private const val REMOTE_BASE_URL = "http://192.168.2.250/api"

    private const val REMOTE_POST_VIEW = "$REMOTE_BASE_URL/posts/view/"
    private const val REMOTE_POST_CREATE = "$REMOTE_BASE_URL/posts/create"
    private const val REMOTE_POST_UPDATE = "$REMOTE_BASE_URL/posts/update/"
    private const val REMOTE_POST_REMOVE = "$REMOTE_BASE_URL/posts/remove/"

    private const val REMOTE_USERS_VIEW = "$REMOTE_BASE_URL/users/view/"
    private const val REMOTE_USERS_PROFILE = "$REMOTE_BASE_URL/users/profile/"
    private const val REMOTE_USERS_REGISTER = "$REMOTE_BASE_URL/users/register"

    private const val REMOTE_UPDATE = REMOTE_BASE_URL + "/users/update"
    private const val REMOTE_UPDATE_FOLLOW = REMOTE_UPDATE + "/follow/"
    private const val REMOTE_UPDATE_UNFOLLOW = REMOTE_UPDATE + "/unfollow/"
    private const val REMOTE_UPDATE_PROFILE_PHOTO = REMOTE_UPDATE + "/profile_photo"
    private const val REMOTE_UPDATE_STARRED_COURSES = REMOTE_UPDATE + "/starred_courses"
    private const val REMOTE_UPDATE_GRADE = REMOTE_UPDATE + "/grade"

    private const val REMOTE_FEED = REMOTE_BASE_URL + "/feed"

    private const val REMOTE_KEY_AUTH = "auth"
    private const val REMOTE_KEY_START = "start"
    private const val REMOTE_KEY_LENGTH = "length"

    private const val KEY_PP_REF = "profilePicRef"
    private const val KEY_STARRED_COURSES = "starredCourses"
    private const val KEY_GRADE = "grade"
    private const val KEY_SCHOOL_NAME = "schoolName"
    private const val KEY_SCHOOL_PLACE_ID = "schoolPlaceId"

    private const val CONTENT_TYPE_JSON = "application/json"

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
                json.put(KEY_STARRED_COURSES, JSONArray(starredCourses.map { it.toString() }))
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

    /**
     * Requests the feed from the server and returns each item as DPost individually, finally
     * calling onComplete() when all the posts have been consumed. Will call onError() if an error
     * occurs during execution.
     */
    @JvmStatic
    fun feed(authToken: String?): Observable<List<DPost>> {
        TODO()
    }
}

/**
 * A data class which holds a server API response. If an error occurred,
 * error != null, and the detailed error will be stored as the response.
 * If the request was successful, payload != null, and will contain the
 * type of request data.
 */
data class RemoteResponse<out T>(val payload: T?, val error: Response?)