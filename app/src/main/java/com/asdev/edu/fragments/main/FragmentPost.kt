package com.asdev.edu.fragments.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.NETWORK_TIMEOUT
import com.asdev.edu.R
import com.asdev.edu.REMOTE_POST_UI
import com.asdev.edu.dialogs.DialogChooseCollection
import com.asdev.edu.dialogs.DialogLike
import com.asdev.edu.models.DCollection
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SharedData
import com.asdev.edu.services.Localization
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class FragmentPost: Fragment() {

    private var post: DPost? = null
    private var subscriptions = CompositeDisposable()

    fun setToPost(post: DPost) {
        this.post = post
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val p = post ?: throw IllegalArgumentException("A post was not provided before onCreateView()")

        val layout = inflater.inflate(R.layout.fragment_post, container, false)

        val img = layout.findViewById<PhotoView>(R.id.fragment_post_img)

        val navBack = layout.findViewById<ImageView>(R.id.fragment_post_nav_back)
        navBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val title = layout.findViewById<TextView>(R.id.fragment_post_title)
        title.text = p.title

        val subtitle = layout.findViewById<TextView>(R.id.fragment_post_subtitle)
        subtitle.text = p.owner?.name?: Localization.convertToTimeString(p.submitTime, requireContext())

        val likes = layout.findViewById<TextView>(R.id.fragment_post_likes)
        val likesContainer = layout.findViewById<FrameLayout>(R.id.fragment_post_likes_container)
        likes.text = p.numLikes.toString()

        val save = layout.findViewById<ImageView>(R.id.fragment_post_save)

        val send = layout.findViewById<ImageView>(R.id.fragment_post_send)

        // check if filled in
        if(p.likes.contains(SharedData.duserRo(requireContext())?.user?._id?: "")) {
            likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp, 0, 0, 0)
            // disable button
            likesContainer.isEnabled = false
        }

        val actionHandler = BehaviorSubject.create<DUIAction<DPost>>()

        // bind actions
        likesContainer.setOnClickListener {
            val user = SharedData.duserRo(requireContext())

            if(user == null) {
                showSnackbar(R.string.error_must_be_signed_in)
                return@setOnClickListener
            }

            // make sure we're not liking are on post
            if(p.ownerId == user.user._id) {
                actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_LIKE, p))
                return@setOnClickListener
            }

            // emit a post like action
            actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_LIKE, p))
            // show as liked button
            likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp, 0, 0, 0)
            // disable the button
            likesContainer.isEnabled = false
            // increment likes count
            likes.text = (p.numLikes + 1).toString()
        }

        send.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_SEND, p))
        }

        save.setOnClickListener {
            actionHandler.onNext(DUIAction(DUIAction.TYPE_POST_SAVE, p))
        }

        val sub = actionHandler.subscribeBy(
                onNext = { action ->
                    if (action.type == DUIAction.TYPE_POST_LIKE) {
                        onPostLike(action.payload)
                    } else if (action.type == DUIAction.TYPE_POST_FULLSCREEN) {
                        val target = FragmentPost()
                        target.setToPost(action.payload)

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
                    } else if(action.type == DUIAction.TYPE_POST_SEND) {
                        // create a share intent
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_TEXT, REMOTE_POST_UI + action.payload._id)
                        requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_post)))
                    } else if(action.type == DUIAction.TYPE_POST_SAVE) {
                        onPostSave(action.payload)
                    }
                }
        )

        subscriptions.add(sub)

        // do a glide fetch into the img view
        Glide.with(img.context)
                .load(p.ref)
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(img.drawable)
                )
                .transition(DrawableTransitionOptions().crossFade(200))
                .into(img)

        layout.postDelayed({
            layout.findViewById<View>(R.id.fragment_post_ad_spot).visibility = View.VISIBLE
        }, 2000)

        return layout
    }

    private fun onPostLike(post: DPost) {
        val user = SharedData.duserRo(requireContext()) ?: return
        if (post.ownerId == user.user._id) {
            showSnackbar(R.string.error_cant_like_own_post)
            return
        }

        println("Submitting like")
        // submit an post like action
        val subscription = RxFirebaseAuth.getToken()
                .subscribeOn(Schedulers.io())
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .flatMap {
                    RemoteService.postLike(it, post._id!!)
                            .subscribeOn(Schedulers.io())
                            .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                        },
                        onNext = {
                            Log.d("OSTTransaction", "The following OST action happened: $it")

                            // show the dialog
                            val dialog = DialogLike.build(requireContext(), post.owner?.name?: "Unknown", post.title, 10.0)
                            dialog.show()
                        },
                        onComplete = {

                        }
                )

        subscriptions.add(subscription)
    }

    private fun onPostSave(post: DPost) {
        var dialog: MaterialDialog? = null

        val callback: (Int) -> Unit = {
            dialog?.dismiss()

            // if 0, then create new
            // if 1, then recently saved

            when(it) {

                0 -> {
                    createAndInsertIntoCollection(post)
                }

                1 -> {
                    insertIntoQuickSaved(post)
                }

                else -> {
                    val collections = SharedData.duserRo(requireContext())?.user?.collections?.sortedByDescending { it.submitTime }
                    val collection = collections?.get(it - 2)

                    if (collection == null) {
                        showSnackbar(R.string.text_invalid_collection)
                    } else {
                        insertIntoCollection(post, collection)
                    }
                }

            }
        }

        // create a dialog to choose
        val builder = DialogChooseCollection.build(requireContext(), callback)


        dialog = builder.show()
    }

    private fun insertIntoCollection(post: DPost, collection: DCollection) {
        // check if its already in it
        if(collection.postRefs.contains(post._id)) {
            showSnackbar(R.string.error_collection_already_contains)
            return
        }

        // call the remote service add collection, then invalidate the local DUser
        // update post refs to include this post
        val newRefs = collection.postRefs.toMutableList().apply { add(post._id!!) }

        var sub: Disposable? = null

        // show loading dialog
        val loading = MaterialDialog.Builder(requireContext())
                .progress(true, 0, false)
                .content(R.string.dialog_loading)
                .cancelable(true)
                .cancelListener {
                    // dispose subscription
                    sub?.dispose()
                }
                .show()

        sub = RxFirebaseAuth.getToken()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap {
                    RemoteService.collectionUpdate(it, collection.uuid, null, newRefs)
                            .subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                            loading.dismiss()
                        },
                        onNext = {
                            // got our DCollection, call a network invalidate
                            if(it.error != null) {
                                println(it.error)
                                showSnackbar(R.string.error_unable_to_add_to_collection)
                            } else if(it.payload != null){
                                showSnackbar(R.string.text_added_to_collection)
                                SharedData.invalidateDuser(requireContext())
                                navigateToCollection(it.payload)
                            }

                            loading.dismiss()
                        },
                        onComplete = {
                        }
                )

        subscriptions.add(sub)
    }

    private fun insertIntoQuickSaved(post: DPost)   {
        SharedData.quickSavedRw(requireContext()) {
            val posts = it.posts?: listOf()
            it.postRefs = it.postRefs.toMutableList().apply { add(post._id!!) }
            it.posts = posts.toMutableList().apply { add(post) }
        }

        // nothing server side about this
    }

    private fun createAndInsertIntoCollection(post: DPost) {

        // stage 2 of the creation
        fun stage2(name: String) {
            var sub: Disposable? = null

            // show loading dialog
            val loading = MaterialDialog.Builder(requireContext())
                    .progress(true, 0, false)
                    .content(R.string.dialog_loading)
                    .cancelable(true)
                    .cancelListener {
                        // dispose subscription
                        sub?.dispose()
                    }
                    .show()

            sub = RxFirebaseAuth.getToken()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .flatMap {
                        RemoteService.collectionCreate(it, name, listOf(post._id!!))
                                .subscribeOn(Schedulers.io())
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                it.printStackTrace()
                                loading.dismiss()
                            },
                            onNext = {
                                // got our DCollection, call a network invalidate
                                if(it.error != null) {
                                    println(it.error)
                                    showSnackbar(R.string.error_unable_to_create_collection)
                                } else if(it.payload != null){
                                    showSnackbar(R.string.text_collection_created)
                                    SharedData.invalidateDuser(requireContext())
                                    navigateToCollection(it.payload)
                                }

                                loading.dismiss()
                            },
                            onComplete = {
                            }
                    )

            subscriptions.add(sub)
        }

        // get a name
        MaterialDialog.Builder(requireContext())
                .title(R.string.title_collection_name)
                .inputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(R.string.hint_collection_title, 0, false) {
                    _, input ->
                    // send a create request
                    stage2(input.toString())
                }
                .negativeText(R.string.dialog_cancel)
                .cancelable(true)
                .show()
    }

    private fun navigateToCollection(coll: DCollection) {
        val target = FragmentCollectionView()
        target.set(TYPE_STANDARD, coll)

        // transition in to it
        requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .replace(R.id.content, target)
                .commit()
    }
}