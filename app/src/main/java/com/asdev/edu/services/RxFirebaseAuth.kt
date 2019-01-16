package com.asdev.edu.services

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * A singleton which integrates [FirebaseAuth] capabilities into a [Observable] friendly
 * RxJava async API.
 */
object RxFirebaseAuth {

    fun getToken() =
        Observable.create<String> {
            emitter ->
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if(user == null) {
                emitter.onError(IllegalStateException("No user signed in to retrieve token for"))
                return@create
            }

            val lock = java.lang.Object()

            // add cancellation by notifying the sleeping thread
            emitter.setCancellable {
                synchronized(lock) {
                    lock.notify()
                }
            }

            val task = user.getIdToken(true)
            // wait for task to complete
            try {
                task.await(lock)
            } catch (e: Exception) {
                // error
                emitter.onError(e)
                return@create
            }

            val result = task.result
            // if successful, trigger on next
            // otherwise error it
            if(task.isSuccessful && task.isComplete && result != null) {
                val token = result.token
                if(token != null) {
                    emitter.onNext(token)
                    emitter.onComplete()
                } else {
                    emitter.onError(IllegalStateException("Unable to retrieve token"))
                }
            } else {
                emitter.onError(task.exception?: Exception("An exception occurred while getting the user token"))
            }
        }.subscribeOn(Schedulers.io())!!

}

// the object class is needed as the Any class doesnt support locking or notifying
fun <T> Task<T>.await(@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") lock: java.lang.Object) {

    addOnCompleteListener {
        // notify the thread of the lock release
        synchronized(lock) {
            lock.notify()
        }
    }

    // stall this tread on the lock
    if(!isComplete) {
        synchronized(lock) {
            lock.wait()
        }
    }
}