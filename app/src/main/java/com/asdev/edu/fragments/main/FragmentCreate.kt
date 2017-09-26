package com.asdev.edu.fragments.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.EXPORT_JPEG_QUALITY
import com.asdev.edu.R
import com.asdev.edu.adapters.CoursesAdapter
import com.asdev.edu.models.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create.*

/**
 * A fragment for the [MainActivity] which displays a post creation UI.
 */
class FragmentCreate : SelectableFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate the home layout
        if(inflater == null)
            return null

        val ctw = ContextThemeWrapper(context, R.style.AppTheme_Light)

        val view = inflater.cloneInContext(ctw).inflate(R.layout.fragment_create, container, false)

        val toolbar = view.findViewById(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_delete_black_24dp)
        toolbar.setNavigationOnClickListener(this::actionReset)

        // setup the form buttons
        // title button
        val titleLayout = view.findViewById(R.id.fragment_create_edit_title)
        titleLayout.setOnClickListener(this::actionEditTitle)
        val courseLayout = view.findViewById(R.id.fragment_create_edit_course)
        courseLayout.setOnClickListener(this::actionEditCourse)
        val visLayout = view.findViewById(R.id.fragment_create_edit_vis)
        visLayout.setOnClickListener(this::actionEditVisibility)
        val docLayout = view.findViewById(R.id.fragment_create_edit_doctype)
        docLayout.setOnClickListener(this::actionEditDocType)

        // set school and grade ui labels
        val schoolLabel = view.findViewById(R.id.fragment_create_school_label) as TextView
        val gradeLabel = view.findViewById(R.id.fragment_create_grade_label) as TextView

        SharedData.duserRo(context) {
            schoolLabel.text = it?.schoolName?: context.getText(R.string.text_none)
            gradeLabel.text = it?.grade?.resolveTitle(context)?: context.getText(R.string.text_none)
        }

        return view
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
    }

    //// Button/action recievers ////

    private fun actionClearPreview(@Suppress("UNUSED_PARAMETER") v: View?) {
        clearPreview()
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
                .start(context, this)
    }

    private fun actionEditTitle(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context).apply {
            title(R.string.text_post_title)

            input(getString(R.string.hint_post_title), postTitle, false) {
                _, input ->
                setPostTitle(input?.toString())
            }
            inputRangeRes(1, 64, R.color.md_red_500)
            inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
            positiveText(R.string.dialog_ok)
            positiveColorAttr(R.attr.colorAccent)
            negativeText(R.string.dialog_cancel)
        }.show()
    }

    private fun actionEditCourse(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context).apply {
            title(R.string.text_course)
            positiveText(R.string.dialog_ok)
            positiveColorAttr(R.attr.colorAccent)
            negativeText(R.string.dialog_cancel)

            SharedData.duserRo(context) {
                var courses = it?.starredCourses?: DUser.blank().starredCourses
                if(courses.isEmpty())
                    courses = DUser.blank().starredCourses

                // TODO: this is broke
                adapter(CoursesAdapter(courses), GridLayoutManager(context, 2))
            }

        }.show()
    }

    private var docType = DDocType.HOMEWORK
    private fun actionEditDocType(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context).apply {
            title(R.string.text_doc_type)
            items(R.array.doc_types)
            itemsCallback {
                _, _, which, text ->
                // translate the index using the which index
                val indicesArray = resources.getStringArray(R.array.doc_types_indices)
                val dt = DDocType.byName(indicesArray[which])?: return@itemsCallback
                docType = dt
                // update the ui
                fragment_create_doctype_label.text = text
            }
        }.show()
    }

    private var visibility = VISIBILITY_PUBLIC
    private fun actionEditVisibility(@Suppress("UNUSED_PARAMETER") v: View?) {
        MaterialDialog.Builder(context).apply {
            title(R.string.text_visibility)
            items(R.array.visibilities)
            itemsCallback {
                _, _, which, text ->
                // update the selected item
                visibility = which
                // update the selected text
                fragment_create_vis_label.text = text
            }
        }.show()
    }

    private fun actionReset(@Suppress("UNUSED_PARAMETER") v: View?) {

    }

    //// Actual post modification methods ////

    private var imageUri: Uri? = null
    private var sourceUri: Uri? = null

    private fun setPreview(uri: Uri, sourceUri: Uri) {
        this.imageUri = uri
        this.sourceUri = sourceUri

        // hide the select image view
        fragment_create_select_image.visibility = View.GONE
        fragment_create_preview_controls.visibility = View.VISIBLE
        fragment_create_preview_image.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE

        // show the preview image
        val previewView = fragment_create_preview_image
        previewView.maximumScale = 3f

        val input = activity.contentResolver.openInputStream(uri)

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
        val bitmap = BitmapFactory.decodeStream(activity.contentResolver.openInputStream(uri), null, options)
        previewView.setImageBitmap(bitmap)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options) =
       if(options.outHeight > 2000 || options.outWidth > 2000) {
           4
       } else if(options.outHeight > 1000 || options.outWidth > 1000) {
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
        fragment_create_title_label.text = title?: getString(R.string.text_none)
        // TODO: set the drawable tint color?
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            // pass on to the create fragment
            if(resultCode == Activity.RESULT_OK) {
                setPreview(result.uri, result.originalUri)
                result.bitmap?.recycle()
                result.originalBitmap?.recycle()
            }
        }
    }

}