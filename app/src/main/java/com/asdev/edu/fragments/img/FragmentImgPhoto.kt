package com.asdev.edu.fragments.img

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.edu.R
import com.google.android.cameraview.CameraView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_img_photo.*

class FragmentImgPhoto: Fragment(), CameraView.PictureReceiver {

    private val RC_REQUEST_CAMERA_PERM = 54643

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inflater?: return null // assert that the inflater is not null

        return inflater.inflate(R.layout.fragment_img_photo, container, false)
    }

    override fun onResume() {
        super.onResume()

        setupCamera()
    }

    override fun onPause() {
        super.onPause()

        // pause camera is running
        if(cameraState) {
            cameraState = false
            fragment_img_camera.stop()
        }
    }

    /**
     * Whether or not this fragment will intercept a back press.
     */
    fun interceptBackPress(): Boolean {
        // check if in preview state.
        if(previewState) {
            actionClearPreview(null)
            return true
        }

        return false
    }

    /**
     * The current camera state. True = camera is open, false = camera is closed.
     */
    private var cameraState = false
    /**
     * Whether or not this fragment is in the preview state.
     */
    private var previewState = false

    private fun setupCamera() {
        if(PermissionChecker.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            fragment_img_overlay.visibility = View.GONE
            fragment_img_camera_content.visibility = View.VISIBLE
            fragment_img_preview_content.visibility = View.GONE

            val camera = fragment_img_camera

            // show camera as is
            if(!cameraState) {
                cameraState = true
                camera.start()
                flashState = camera.flash
                camera.removeAllCallbacks()
                camera.addCallback(CameraView.PictureCallback(this))
            }

            fragment_img_flash.setOnClickListener(this::actionFlash)
            fragment_img_shutter.setOnClickListener(this::actionShutter)

        } else {
            // show no camera ui
            fragment_img_overlay.visibility = View.VISIBLE
            fragment_img_camera_content.visibility = View.GONE
            fragment_img_preview_content.visibility = View.GONE

            // setup button callbacks
            fragment_img_perm_button.setOnClickListener(this::actionRequestCameraPerm)
        }
    }

    private fun actionRequestCameraPerm(@Suppress("UNUSED_PARAMETER") v: View?) {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), RC_REQUEST_CAMERA_PERM)
    }

    private var flashState = CameraView.FLASH_AUTO
    private fun actionFlash(@Suppress("UNUSED_PARAMETER") v: View?) {
        // change to the next state
        flashState = when(flashState) {
            CameraView.FLASH_AUTO -> CameraView.FLASH_OFF
            CameraView.FLASH_OFF -> CameraView.FLASH_ON
            CameraView.FLASH_ON -> CameraView.FLASH_AUTO
            else -> CameraView.FLASH_AUTO
        }

        // update the camera mode
        fragment_img_camera.flash = flashState

        // update the drawable
        val drawable = when(flashState) {
            CameraView.FLASH_AUTO -> R.drawable.ic_flash_auto_white_24dp
            CameraView.FLASH_OFF -> R.drawable.ic_flash_off_white_24dp
            CameraView.FLASH_ON -> R.drawable.ic_flash_on_white_24dp
            else -> R.drawable.ic_flash_auto_white_24dp
        }

        fragment_img_flash.setImageResource(drawable)
    }

    private fun actionShutter(@Suppress("UNUSED_PARAMETER") v: View?) {
        fragment_img_camera.takePicture()
    }

    private fun actionClearPreview(@Suppress("UNUSED_PARAMETER") v: View?) {
        if(previewState) {
            // recycle the current preview content if it exists
            fragment_img_preview.setImageBitmap(null)
            // destroy preview bitmap
            previewBitmap?.recycle()
            previewBitmap = null

            // change the state to camera state
            previewState = false
            setupCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == RC_REQUEST_CAMERA_PERM) {
            // reset the camera
            setupCamera()
        }
    }

    /**
     * The current preview bitmap, stored to be later recycled.
     */
    private var previewBitmap: Bitmap? = null

    override fun onPictureTaken(cameraView: CameraView, jpegData: ByteArray) {
        // pause the camera
        cameraView.stop()
        cameraState = false
        previewState = true
        // show the image preview layout
        fragment_img_preview_content.visibility = View.VISIBLE
        // hide the other camera layout to prevent overdraw
        fragment_img_camera_content.visibility = View.GONE

        // setup the buttons
        fragment_img_preview_clear.setOnClickListener(this::actionClearPreview)

        // down sample the image
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inSampleSize = 4

        // decode the data and upload to the preview view
        val bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, options)
        fragment_img_preview.setImageBitmap(bitmap)
        previewBitmap = bitmap
    }

}