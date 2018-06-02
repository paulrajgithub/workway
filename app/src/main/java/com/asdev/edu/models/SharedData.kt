package com.asdev.edu.models

import android.content.Context
import com.asdev.edu.*
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * A singleton which holds common data between the entire
 * app lifecycle.
 */
object SharedData {

    private var duser: UserBundle? = null
    private var isDuserNull = false

    private var subscription: Disposable? = null
    private var isInvalid = true // initially always invalid

    private var quickSaved: DCollection? = null
    private var isQuickSavedNull = false

    private fun init(appContext: Context) {
        duser = readJsonFile(appContext, DUSER_FILE)
        isDuserNull = duser == null
    }

    private fun initQs(appContext: Context) {
        quickSaved = readJsonFile(appContext, QUICK_SAVED_FILE)
        isQuickSavedNull = quickSaved == null
    }

    /**
     * Returns the DUser object as a read-only object. Changes will
     * not be committed to the disk. Any changes made to the returned
     * object will be discarded.
     */
    fun duserRo(appContext: Context): UserBundle? {
        if(isDuserNull)
            return null
        if(duser != null)
            return duser?.copy()

        // reload cuz duser got destroyed somehow
        init(appContext)
        return duser?.copy()
    }

    /**
     * Gets the DUser as a read-only object that will be passed in as
     * a parameter to the given block of code. Any changes made to the
     * provided object will be discarded.
     */
    fun duserRo(appContext: Context, block: (UserBundle?) -> Unit) {
        block(duserRo(appContext))
    }

    /**
     * Takes in a block of code with the param of the DUser object.
     * After the block is executed, the changes to the object
     * will be committed to the disk. THIS WILL NOT UPDATE
     * ON THE SERVER SIDE.
     */
    fun duserRw(appContext: Context, action: (UserBundle?) -> Unit) {
        if(!isDuserNull && duser == null) {
            init(appContext) // reload duser from disk
        }

        action(duser)

        duser?.let {
            commitJsonFile(it, appContext, DUSER_FILE)
        }
    }

    fun quickSavedRo(appContext: Context): DCollection? {
        if(isQuickSavedNull)
            return null
        if(quickSaved != null)
            return quickSaved

        initQs(appContext)
        return quickSaved
    }

    fun quickSavedRw(appContext: Context, action: (DCollection) -> Unit) {
        var qs = quickSavedRo(appContext)

        if(qs == null)
            qs = DCollection("qs", System.currentTimeMillis(), appContext.getString(R.string.text_quick_save_collection), listOf(), listOf(), duserRo(appContext)!!.user)

        action(qs)

        commitJsonFile(qs, appContext, QUICK_SAVED_FILE)
    }

    fun invalidateDuser(appContext: Context) {
        // trigger a network update, and create the subscription
        isInvalid = true
        // on resume will trigger a network update, assuming this state is invalid
        onResume(appContext)
    }

    fun invalidateDuser(appContext: Context, postAction: (UserBundle) -> Unit) {
        isInvalid = true
        onResume(appContext, postAction)
    }

    fun onResume(appContext: Context, postAction: (UserBundle) -> Unit = {}) {
        // check if the invalidation was completed, if not,
        // attempt it again
        if(isInvalid) {
            subscription = networkUpdate(appContext)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onNext = {
                                // make sure to reset the invalidation flag
                                isInvalid = false
                                postAction(it)
                            },
                            onError = {
                                it.printStackTrace()
                            }
                    )
        }
    }

    fun onPause() {
        // dispose the subscription, cancelling any operations
        subscription?.dispose()
    }

    /**
     * Updates the current duser object with one retrieved from the server.
     * Returns an Observable which can be used to access the update
     * duser object.
     */
    fun networkUpdate(appContext: Context): Observable<UserBundle> {
        return Observable.create<UserBundle> {
            emitter ->

            val subscription =
                    RxFirebaseAuth.getToken()
                            .subscribeOn(Schedulers.io())
                            .flatMap {
                                RemoteService.userRetrieve(it).subscribeOn(Schedulers.io())
                            }
                            .observeOn(Schedulers.computation())
                            .subscribeBy(
                                    onError = {
                                        emitter.onError(it)
                                    },
                                    onComplete = {
                                        emitter.onComplete()
                                    },
                                    onNext = {
                                        if(it.error != null && it.payload == null) {
                                            // emit an error
                                            emitter.onError(ResponseException(it.error))
                                        } else if(it.payload != null){
                                            // update self duser instance
                                            this.duser = it.payload
                                            // good response, commit the disk duser
                                            commitJsonFile(it.payload, appContext, DUSER_FILE)
                                            // emit a next
                                            emitter.onNext(it.payload)
                                        }
                                    }
                            )

            // set our disposable object as the remote disposable
            emitter.setDisposable(subscription)
        }
    }
}