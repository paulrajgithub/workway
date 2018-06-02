package com.asdev.edu.services

import com.androidnetworking.error.ANError
import com.asdev.edu.DEFAULT_HOME_FEED_LEN
import com.asdev.edu.GSON
import com.asdev.edu.models.*
import com.asdev.ost.sdk.models.OSTTransaction
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
    private const val REMOTE_POST_LIKE = "$REMOTE_BASE_URL/posts/like/"

    private const val REMOTE_USERS_VIEW = "$REMOTE_BASE_URL/users/view/"
    private const val REMOTE_USERS_PROFILE = "$REMOTE_BASE_URL/users/profile"
    private const val REMOTE_USERS_REGISTER = "$REMOTE_BASE_URL/users/register"

    private const val REMOTE_UPDATE = "$REMOTE_BASE_URL/users/update"
    private const val REMOTE_UPDATE_FOLLOW = "$REMOTE_UPDATE/follow/"
    private const val REMOTE_UPDATE_UNFOLLOW = "$REMOTE_UPDATE/unfollow/"
    private const val REMOTE_UPDATE_PROFILE_PHOTO = "$REMOTE_UPDATE/profile_photo"
    private const val REMOTE_UPDATE_STARRED_COURSES = "$REMOTE_UPDATE/starred_courses"
    private const val REMOTE_UPDATE_GRADE = "$REMOTE_UPDATE/grade"

    private const val REMOTE_UPDATE_COLL = "$REMOTE_UPDATE/update_collection/"
    private const val REMOTE_UPDATE_CREATE_COLL = "$REMOTE_UPDATE/new_collection"

    private const val REMOTE_COLLECTION_GET = "$REMOTE_BASE_URL/collections/"

    private const val REMOTE_FEED = "$REMOTE_BASE_URL/feed"

    private const val REMOTE_KEY_AUTH = "auth"
    private const val REMOTE_KEY_START = "start"
    private const val REMOTE_KEY_LENGTH = "length"

    private const val KEY_PP_REF = "profilePicRef"
    private const val KEY_STARRED_COURSES = "starredCourses"
    private const val KEY_GRADE = "grade"
    private const val KEY_SCHOOL_NAME = "schoolName"
    private const val KEY_SCHOOL_PLACE_ID = "schoolPlaceId"
    private const val KEY_NAME = "name"

    private const val KEY_TITLE = "title"
    private const val KEY_REF = "ref"
    private const val KEY_TAGS = "tags"
    private const val KEY_VISIBILITY = "visibility"

    private const val KEY_POST_REFS = "postRefs"

    private const val KEY_TAG_ID = "id"
    private const val KEY_TAG_SCOPE = "scope"
    private const val KEY_TAG_TEXT = "text"

    private const val CONTENT_TYPE_JSON = "application/json"

    /**
     * Registers a new user on the remote service with the given properties.
     * Returns an observable, which on completion, will return a RemoteResponse sent
     * by the server.
     */
    @JvmStatic
    fun userRegister(authToken: String, name: String, profilePicRef: String?, starredCourses: List<DCourse>?, grade: DGrade, school: DSchool): Observable<RemoteResponse<UserBundle>> {
        return Observable.create<RemoteResponse<UserBundle>> { emitter ->

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
            json.put(KEY_NAME, name)

            // now we have our post content, build our actual request
            val subscription = Rx2AndroidNetworking.post(REMOTE_USERS_REGISTER)
                    .addJSONObjectBody(json) // add json body
                    .setContentType(CONTENT_TYPE_JSON) // set to application/json content type
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val user = GSON.fromJson(it.toString(0), UserBundle::class.java)
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
     * Uploads the given post object on the remote service with the given properties.
     * Returns an observable, which on completion, will return a RemoteResponse sent
     * by the server.
     */
    @JvmStatic
    fun postCreate(authToken: String, title: String, ref: String, tags: List<DTag>, visibility: Int = VISIBILITY_PUBLIC): Observable<RemoteResponse<DPost>> {
        // query param of auth
        return Observable.create<RemoteResponse<DPost>> { emitter ->
            // build the json object
            val json = JSONObject()
            // put the parameters
            json.put(KEY_TITLE, title)
            json.put(KEY_REF, ref)
            // build a json array of tags
            val tagsJson = JSONArray()

            for (tag in tags) {
                val tagJson = JSONObject()
                tagJson.put(KEY_TAG_ID, tag.id)
                tagJson.put(KEY_TAG_SCOPE, tag.scope)
                tagJson.put(KEY_TAG_TEXT, tag.text)
                // append to the array
                tagsJson.put(tagJson)
            }

            json.put(KEY_TAGS, tagsJson)
            json.put(KEY_VISIBILITY, visibility)

            // now we have our post content, build our actual request
            val subscription = Rx2AndroidNetworking.post(REMOTE_POST_CREATE)
                    .addJSONObjectBody(json) // add json body
                    .setContentType(CONTENT_TYPE_JSON) // set to application/json content type
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val post = GSON.fromJson(it.toString(0), DPost::class.java)
                                    emitter.onNext(RemoteResponse(post, null))
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
     * Submits a request to
     */
    @JvmStatic
    fun postLike(authToken: String, postId: String): Observable<RemoteResponse<OSTTransaction>> {
        return Observable.create { emitter ->
            // no need for a json object, only authentication
            val subscription = Rx2AndroidNetworking.get(REMOTE_POST_LIKE + postId)
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val transaction = GSON.fromJson(it.toString(0), OSTTransaction::class.java)
                                    emitter.onNext(RemoteResponse(transaction, null))
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
     * Retrieves the DUser object from the user, which will always the be most up-to-date version
     * and returns it.
     */
    @JvmStatic
    fun userRetrieve(authToken: String): Observable<RemoteResponse<UserBundle>> {
        return Observable.create { emitter ->
            // no need for a json object, only authentication
            val subscription = Rx2AndroidNetworking.get(REMOTE_USERS_PROFILE)
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val userBundle = GSON.fromJson(it.toString(0), UserBundle::class.java)
                                    emitter.onNext(RemoteResponse(userBundle, null))
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

    @JvmStatic
    fun collectionCreate(authToken: String, name: String, postRefs: List<String>): Observable<RemoteResponse<DCollection>> {
        return Observable.create { emitter ->
            // build the json object
            val json = JSONObject()
            // put the parameters
            json.put(KEY_NAME, name)

            val postRefsJson = JSONArray(postRefs)

            json.put(KEY_POST_REFS, postRefsJson)

            // now we have our post content, build our actual request
            val subscription = Rx2AndroidNetworking.post(REMOTE_UPDATE_CREATE_COLL)
                    .addJSONObjectBody(json) // add json body
                    .setContentType(CONTENT_TYPE_JSON) // set to application/json content type
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val coll = GSON.fromJson(it.toString(0), DCollection::class.java)
                                    emitter.onNext(RemoteResponse(coll, null))
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

    fun collectionUpdate(authToken: String, uuid: String, name: String? = null, postRefs: List<String>? = null): Observable<RemoteResponse<DCollection>> {
        return Observable.create { emitter ->
            // build the json object
            val json = JSONObject()

            // put the parameters
            if(name != null)
                json.put(KEY_NAME, name)

            if(postRefs != null) {
                val postRefsJson = JSONArray(postRefs)
                json.put(KEY_POST_REFS, postRefsJson)
            }

            // now we have our post content, build our actual request
            val subscription = Rx2AndroidNetworking.post(REMOTE_UPDATE_COLL + uuid)
                    .addJSONObjectBody(json) // add json body
                    .setContentType(CONTENT_TYPE_JSON) // set to application/json content type
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val coll = GSON.fromJson(it.toString(0), DCollection::class.java)
                                    emitter.onNext(RemoteResponse(coll, null))
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

    fun collectionGet(uuid: String): Observable<RemoteResponse<DCollection>> {
        return Observable.create<RemoteResponse<DCollection>> {
            emitter ->
            val sub = Rx2AndroidNetworking.get(REMOTE_COLLECTION_GET + uuid)
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val coll = GSON.fromJson(it.toString(0), DCollection::class.java)
                                    emitter.onNext(RemoteResponse(coll, null))
                                    emitter.onComplete()
                                } catch (e: Exception) {
                                    emitter.onError(e)
                                }
                            }
                    )

            // can cancel the request
            emitter.setDisposable(sub)
        }

    }

    /**
     * Requests the feed from the server and returns a UserFeed object, finally
     * calling onComplete() when all the posts have been consumed. Will call onError() if an error
     * occurs during execution.
     */
    @JvmStatic
    fun feed(authToken: String?, offset: Int = 0, length: Int = DEFAULT_HOME_FEED_LEN): Observable<RemoteResponse<UserFeed>> {
        return Observable.create { emitter ->
            // no need for a json object, only authentication
            val subscription = Rx2AndroidNetworking.get(REMOTE_FEED)
                    .addQueryParameter(REMOTE_KEY_AUTH, authToken) // add auth token query param
                    .addQueryParameter(REMOTE_KEY_START, offset.toString())
                    .addQueryParameter(REMOTE_KEY_LENGTH, length.toString())
                    .build()
                    .jsonObjectObservable
                    .subscribeBy(
                            onError = { resp ->
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
                                    val feed = GSON.fromJson(it.toString(0), UserFeed::class.java)
                                    emitter.onNext(RemoteResponse(feed, null))
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

/**
 * A data class which holds a server API response. If an error occurred,
 * error != null, and the detailed error will be stored as the response.
 * If the request was successful, payload != null, and will contain the
 * type of request data.
 */
data class RemoteResponse<out T>(val payload: T?, val error: Response?)