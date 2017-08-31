package com.asdev.edu.services

import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.UploadProgressListener
import com.asdev.edu.NETWORK_TIMEOUT
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

const val KUUV_UPLOAD_URL = "http://kuuv.io/upload.php"
const val KUUV_KEY_IMG = "upl"
const val KUUV_UPLOAD_TAG = "kuuv_img_upl"

/**
 * An object to interact with the Kuuv.io service.
 */
object KuuvService {

    /**
     * Returns an observable which when subscribed, will return the url of the uploaded image
     * on success.
     */
    fun upload(file: File, listener: (Long, Long) -> Unit): Observable<String> {
        // build a request
        return Rx2AndroidNetworking.upload(KUUV_UPLOAD_URL)
                .addMultipartFile(KUUV_KEY_IMG, file)
                .setPriority(Priority.HIGH)
                .setTag(KUUV_UPLOAD_TAG)
                .build()
                .setUploadProgressListener(listener)
                .stringObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
    }

}
