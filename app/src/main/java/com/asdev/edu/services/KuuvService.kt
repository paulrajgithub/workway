package com.asdev.edu.services

import com.androidnetworking.common.Priority
import com.asdev.edu.NETWORK_TIMEOUT
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment


const val KUUV_UPLOAD_URL = "https://kuuv.io/upload.php"
const val KUUV_KEY_IMG = "upl"
const val KUUV_UPLOAD_TAG = "kuuv_img_upl"

/**
 * A single to interact with the Kuuv.io service. Acts as a transport bridge and is fully concurrent,
 * using RxJava's [Observable]s.
 */
object KuuvService {

    /**
     * Returns an observable which when subscribed, will return the url of the uploaded image
     * on success.
     */
    fun upload(context: Context, uri: Uri, listener: (Long, Long) -> Unit) = upload(File(getFilePath(context, uri)), listener)

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
                // will return a string containing the image URL
                .stringObservable
                // run on a dedicated io thread
                .subscribeOn(Schedulers.io())
                // join on main thread
                .observeOn(AndroidSchedulers.mainThread())
                // set the standard network timeout
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
    }

    // resolving android Uri to file paths
    private fun getFilePath(context: Context, uriIn: Uri): String? {
        var uri = uriIn
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Environment.getExternalStorageDirectory().path + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor
            try {
                cursor = context.contentResolver
                        .query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    cursor.close()
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

}
