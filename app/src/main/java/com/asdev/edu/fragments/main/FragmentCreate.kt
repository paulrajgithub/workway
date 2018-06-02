package com.asdev.edu.fragments.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.*
import com.asdev.edu.adapters.CoursesAdapter
import com.asdev.edu.models.*
import com.asdev.edu.services.KuuvService
import com.asdev.edu.services.Localization
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import com.github.florent37.viewtooltip.ViewTooltip
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create.*
import java.text.NumberFormat

private const val COURSE_ITEMS_NUM = 3

/**
 * A fragment for the [MainActivity] which displays a post creation UI.
 */
class FragmentCreate : SelectableFragment() {

    private var subscriptions = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        // inflate the home layout
        val ctw = ContextThemeWrapper(context, R.style.AppTheme_Light)

        val view = inflater.cloneInContext(ctw).inflate(R.layout.fragment_create, container, false)

        val toolbar = view.findViewById(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_delete_black_24dp)
        toolbar.setNavigationOnClickListener(this::actionReset)
        toolbar.inflateMenu(R.menu.fragment_create)
        toolbar.setOnMenuItemClickListener(this::onMenuItemClicked)

        // setup the form buttons
        // title button
        val titleLayout = view.findViewById(R.id.fragment_create_edit_title) as LinearLayout
        titleLayout.setOnClickListener(this::actionEditTitle)
        val courseLayout = view.findViewById(R.id.fragment_create_edit_course) as LinearLayout
        courseLayout.setOnClickListener(this::actionEditCourse)
        val visLayout = view.findViewById(R.id.fragment_create_edit_vis) as LinearLayout
        visLayout.setOnClickListener(this::actionEditVisibility)
        val docLayout = view.findViewById(R.id.fragment_create_edit_doctype) as LinearLayout
        docLayout.setOnClickListener(this::actionEditDocType)

        // set school and grade ui labels
        val schoolLabel = view.findViewById(R.id.fragment_create_school_label) as TextView
        val gradeLabel = view.findViewById(R.id.fragment_create_grade_label) as TextView

        SharedData.duserRo(context!!) {
            it ?: return@duserRo
            schoolLabel.text = it.user.schoolName
            gradeLabel.text = it.user.grade.resolveTitle(context!!)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.dispose()
    }

    //// Lifecycle receivers ////

    override fun onSelected() {
    }

    override fun onReselected() {
    }

    override fun onResume() {
        super.onResume()

        fragment_create_select_image.setOnClickListener(this::actionSelectImage)
        fragment_create_preview_clear.setOnClickListener(this::actionClearPreview)
        fragment_create_preview_add_img.setOnClickListener(this::actionSelectAdditionalImage)
        fragment_create_preview_recrop.setOnClickListener(this::actionRecrop)
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_upload -> {
            actionUpload()
            true
        }

        else -> false
    }

    //// Button/action recievers ////

    private fun actionUpload() {
        // make sure the user is signed in
        val user = FirebaseAuth.getInstance().currentUser

        if(user == null) {
            showSnackbar(R.string.error_must_be_signed_in)
            return
        }

        var uploadDialog: MaterialDialog? = null
        var subscription: Disposable? = null

        val iUri = imageUri
        val title = postTitle
        val course = course

        if (iUri == null) {
            showSnackbar(R.string.error_invalid_image)
            return
        }

        if (title == null) {
            showSnackbar(R.string.error_must_set_title)
            return
        }

        if (course == null) {
            showSnackbar(R.string.error_must_set_course)
            return
        }

        // create a progress dialog
        uploadDialog = MaterialDialog.Builder(context!!).apply {
            title(R.string.title_uploading_post)
            progress(false, 100, false)
            progressPercentFormat(NumberFormat.getPercentInstance())

            negativeText(R.string.text_cancel)
            // cancel on negative button pressed
            onNegative { _, _ -> uploadDialog?.cancel() }

            cancelable(true)
            // cancel the upload by cancelling the subscription
            cancelListener {
                subscription?.dispose()
            }

            canceledOnTouchOutside(false)
        }.build()

        uploadDialog?.show()

        var imgUrl: String? = null

        // upload the current file to kuuv, and post meta data after
        val observable =
                KuuvService.upload(context!!, iUri) { bytes, totalBytes ->
                    // calculate number percentage
                    // 90% of the process is uploading the file,
                    // the 10% is uploading metadata
                    val percentage = (bytes.toDouble() / totalBytes.toDouble()) * 90.0
                    uploadDialog?.setProgress(percentage.toInt())
                }
                .subscribeOn(Schedulers.io())
                .flatMap {
                    imgUrl = it
                    RxFirebaseAuth.getToken().subscribeOn(Schedulers.io())
                }
                .flatMap { token ->
                    println("Got token")
                    uploadDialog?.setProgress(95)

                    val user = SharedData.duserRo(context!!)!!
                    val grade = user.user.grade
                    val school = DSchool(user.user.schoolName, user.user.schoolPlaceId)
                    val tags = listOf(course.toTag(context!!), docType.toTag(context!!), grade.toTag(context!!), school.toTag())
                    RemoteService.postCreate(token, title, imgUrl!!, tags, visibility).subscribeOn(Schedulers.io())
                }

        subscription = observable.subscribeBy(
                onError = {
                    it.printStackTrace()
                },
                onComplete = {
                    uploadDialog?.setProgress(100)
                    uploadDialog?.dismiss()
                    // invalidate the duser object
                    SharedData.invalidateDuser(activity!!.applicationContext)
                },
                onNext = {
                    if(it.error != null && it.payload == null) {
                        // Display the error msg
                        val errorMsg = Localization.getResponseMsg(it.error)
                        showSnackbar(errorMsg)
                    } else if(it.payload != null){
                        // SUCCESS
                        val target = FragmentPost()
                        target.setToPost(it.payload)

                        // do a fragment transition
                        requireActivity()
                                .supportFragmentManager
                                .beginTransaction()
                                // .addSharedElement(action.sharedElement!!, "post_image_target")
                                // .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_enter, R.anim.fragment_exit)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .addToBackStack(null)
                                .replace(R.id.content, target)
                                .commit()
                    }
                }
        )

        // add to composite
        subscriptions.add(subscription)
    }

    private fun actionClearPreview(@Suppress("UNUSED_PARAMETER") v: View?) {
        clearPreview()
    }

    private fun actionFullscreenPreview(@Suppress("UNUSED_PARAMETER") v: View?) {
        // TODO: shared element transition into a full screen image view activity
        // pass image uri(s) as extra data
    }

    private fun actionSelectImage(@Suppress("UNUSED_PARAMETER") v: View?) {
        // launch the crop activity which will also ask for an image source
        CropImage.activity()
                .setAllowFlipping(false)
                .setAllowRotation(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(EXPORT_JPEG_QUALITY)
                .setBackgroundColor(Color.parseColor("#55EEEEEE"))
                .setActivityMenuIconColor(Color.parseColor("#202020"))
                .start(context!!, this)
    }

    private fun actionSelectAdditionalImage(@Suppress("UNUSED_PARAMETER") v: View?) {
        val intent = CropImage.activity()
                .setAllowFlipping(false)
                .setAllowRotation(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(EXPORT_JPEG_QUALITY)
                .setBackgroundColor(Color.parseColor("#55EEEEEE"))
                .setActivityMenuIconColor(Color.parseColor("#202020"))
                .getIntent(context!!)

        startActivityForResult(intent, RC_ADDITIONAL_IMAGE_PICKER)
    }

    private fun actionEditTitle(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context!!).apply {
            title(R.string.text_post_title)

            input(getString(R.string.hint_post_title), postTitle, false) { _, input ->
                setPostTitle(input?.toString())
            }

            inputRangeRes(1, 64, R.color.md_red_500)
            inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
            positiveText(R.string.dialog_ok)
            positiveColorAttr(R.attr.colorAccent)
            negativeText(R.string.dialog_cancel)
        }.show()
    }

    private var courseDialog: MaterialDialog? = null
    private fun actionEditCourse(@Suppress("UNUSED_PARAMETER") v: View?) {
        courseDialog = MaterialDialog.Builder(context!!).apply {
            title(R.string.text_course)
            negativeText(R.string.dialog_cancel)

            // set the adapter to the courses
            SharedData.duserRo(context) {
                adapter(CoursesAdapter(getCoursesInPriority(it?.user), this@FragmentCreate::setCourse), GridLayoutManager(context, COURSE_ITEMS_NUM))
            }

            backgroundColorRes(R.color.colorBackground)

        }.show()
    }

    private var docType = DDocType.HOMEWORK
    private fun actionEditDocType(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context!!).apply {
            title(R.string.text_doc_type)
            items(R.array.doc_types)
            itemsCallback { _, _, which, text ->
                // translate the index using the which index
                val indicesArray = resources.getStringArray(R.array.doc_types_indices)
                val dt = DDocType.byName(indicesArray[which]) ?: return@itemsCallback
                docType = dt
                // update the ui
                fragment_create_doctype_label.text = text
            }
        }.show()
    }

    private var visibility = VISIBILITY_PUBLIC
    private fun actionEditVisibility(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context!!).apply {
            title(R.string.text_visibility)
            items(R.array.visibilities)
            itemsCallback { _, _, which, text ->
                // update the selected item
                visibility = which
                // update the selected text
                fragment_create_vis_label.text = text
            }
        }.show()
    }

    private fun actionReset(@Suppress("UNUSED_PARAMETER") v: View?) {
        // TODO: clear local bitmap copy cache
    }

    private fun actionRecrop(@Suppress("UNUSED_PARAMETER") v: View?) {
        // relaunch crop activity with the original image uri
        CropImage.activity(sourceUri)
                .setAllowFlipping(false)
                .setAllowRotation(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(EXPORT_JPEG_QUALITY)
                .setBackgroundColor(Color.parseColor("#55EEEEEE"))
                .setActivityMenuIconColor(Color.parseColor("#202020"))
                .start(context!!, this)
    }

    //// Actual post modification methods ////

    private var imageUri: Uri? = null
    private var sourceUri: Uri? = null

    private fun setPreview(uri: Uri, sourceUri: Uri) {
        this.imageUri = uri
        this.sourceUri = sourceUri

        val previewIv = fragment_create_preview_image

        // hide the select image view
        fragment_create_select_image.visibility = View.GONE
        fragment_create_preview_controls.visibility = View.VISIBLE
        previewIv.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE

        val input = activity!!.contentResolver.openInputStream(uri)

        // first decode size
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(input, null, boundsOptions)

        // down sample to reduce the memory usage
        val options = BitmapFactory.Options()
        options.inSampleSize = calculateInSampleSize(boundsOptions)
        Log.d("BitmapScaler", "Using a sample size of ${options.inSampleSize} for PhotoView")
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeStream(activity!!.contentResolver.openInputStream(uri), null, options)
        previewIv.setImageBitmap(bitmap)

        // show the tooltip
        // TODO: only if necessary
        ViewTooltip.on(this, previewIv).apply {
            autoHide(true, 2500)
            clickToHide(true)
            align(ViewTooltip.ALIGN.CENTER)
            position(ViewTooltip.Position.BOTTOM)
            text(getString(R.string.tooltip_tap_to_fullscreen))
            color(ContextCompat.getColor(context!!, R.color.colorAccent))
            textColor(Color.parseColor("#FFFFFF"))
        }.show()

        previewIv.setOnClickListener(this::actionFullscreenPreview)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options) =
            if (options.outHeight > 2000 || options.outWidth > 2000) {
                4
            } else if (options.outHeight > 1000 || options.outWidth > 1000) {
                2
            } else {
                1
            }

    private fun clearPreview() {
        fragment_create_preview_controls.visibility = View.GONE
        fragment_create_preview_image.visibility = View.GONE
        divider.visibility = View.GONE
        fragment_create_select_image.visibility = View.VISIBLE

        // destroy old bitmaps
        val previewView = fragment_create_preview_image
        val drawable = previewView.drawable
        previewView.setImageDrawable(null)
        (drawable as? BitmapDrawable)?.bitmap?.recycle()
    }

    private var postTitle: String? = null
    private fun setPostTitle(title: String?) {
        // set the actual local var
        postTitle = title
        // update the UI
        fragment_create_title_label.text = title ?: getString(R.string.text_none)
        // TODO: set the drawable tint color?
    }

    private var course: DCourse? = null
    private fun setCourse(course: DCourse) {
        // dimiss course dialog if shown
        courseDialog?.apply {
            if (isShowing) {
                dismiss()
            }

            this@FragmentCreate.course = course
        }

        // update the ui
        fragment_create_course_label.text = course.resolveTitle(context!!)
    }

    private fun showSnackbar(@StringRes msgRes: Int) {
        showSnackbar(getString(msgRes))
    }

    private fun showSnackbar(msg: String) {
        val v = view?: return
        Snackbar.make(v, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            // pass on to the create fragment
            if (resultCode == Activity.RESULT_OK) {
                // trash intent data
                result.bitmap?.recycle()
                result.originalBitmap?.recycle()
                setPreview(result.uri, result.originalUri)
            }
        } else if (requestCode == RC_ADDITIONAL_IMAGE_PICKER) {
            // TODO: process additional image
        }
    }

}