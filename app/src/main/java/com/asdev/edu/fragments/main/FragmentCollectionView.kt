package com.asdev.edu.fragments.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.NETWORK_TIMEOUT
import com.asdev.edu.R
import com.asdev.edu.REMOTE_COLLECTIONS_UI
import com.asdev.edu.adapters.AdapterPosts
import com.asdev.edu.dialogs.DialogLike
import com.asdev.edu.models.DCollection
import com.asdev.edu.models.DPost
import com.asdev.edu.models.DUIAction
import com.asdev.edu.models.SharedData
import com.asdev.edu.services.Localization
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

const val TYPE_STANDARD = 0
const val TYPE_QUICK_SAVED = 1
const val TYPE_RECENTLY_LIKED = 2
const val TYPE_RECENTLY_VIEWED = 3

class FragmentCollectionView: Fragment() {

    private var collection: DCollection? = null
    private var type = TYPE_STANDARD

    private var subscriptions = CompositeDisposable()

    fun set(type: Int = TYPE_STANDARD, collection: DCollection?) {
        this.type = type
        this.collection = collection
    }

    private fun beginLoadAsync(layout: View) {
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        val coll = collection

        if(type == TYPE_STANDARD) {
            coll?: throw IllegalArgumentException("The collection cannot be null when the type is standard.")

            // set the title and subtitle
            layout.findViewById<TextView>(R.id.fragment_collection_view_title).text = coll.name
            layout.findViewById<TextView>(R.id.fragment_collection_view_subtitle).text = coll.creator?.name?: Localization.convertToTimeString(coll.submitTime, requireContext())

            // bind buttons
            layout.findViewById<ImageView>(R.id.fragment_collection_view_delete).setOnClickListener {
                onDelete()
            }

            layout.findViewById<ImageView>(R.id.fragment_collection_view_edit).setOnClickListener {
                onEdit()
            }

            layout.findViewById<ImageView>(R.id.fragment_collection_view_send).setOnClickListener {
                onSend()
            }

            // begin the batch fetch of the collection
            val sub = RxFirebaseAuth.getToken()
                    .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        RemoteService.collectionGet(coll.uuid)
                                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                it.printStackTrace()
                                showSnackbar(R.string.error_unable_to_load_collection)
                                dismissLoading()
                            },
                            onNext = {
                                if(it.error != null) {
                                    showSnackbar(R.string.error_unable_to_load_collection)
                                } else if(it.payload != null) {
                                    load(it.payload)
                                }
                                dismissLoading()
                            },
                            onComplete = {

                            }
                    )

            subscriptions.add(sub)
        }
    }

    private fun load(collection: DCollection) {
        // set the recycler to the posts
        val recycler = view?.findViewById<RecyclerView>(R.id.fragment_collection_view_recycler)?: return

        val subject = BehaviorSubject.create<DUIAction<DPost>>()

        val sub = subject.subscribeBy {
            if(it.type == DUIAction.TYPE_POST_FULLSCREEN) {
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
            } else if(type == DUIAction.TYPE_POST_LIKE){
                onPostLike(it.payload)
            } else if(type == DUIAction.TYPE_POST_SEND) {
                // create a share intent
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, it.payload.ref)
                requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_post)))
            }
        }

        subscriptions.add(sub)

        val posts = collection.posts?.toMutableList()?: return

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = AdapterPosts(posts, subject)
    }

    private fun dismissLoading() {
        view?.findViewById<ProgressBar>(R.id.fragment_collection_view_loading)?.visibility = View.GONE
    }

    private fun cleanup() {
        subscriptions.dispose()
        subscriptions = CompositeDisposable()
    }

    private fun onDelete() {

        fun delete() {

        }

        MaterialDialog.Builder(requireContext())
                .title(R.string.dialog_are_you_sure)
                .negativeText(R.string.dialog_cancel)
                .positiveText(R.string.dialog_yes)
                .onPositive { dialog, which -> delete() }
                .cancelable(true)
                .build()
                .show()
    }

    private fun onEdit() {

        val coll = collection?: return

        fun changeName(name: String) {
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
                        RemoteService.collectionUpdate(it, coll.uuid, name)
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
                                    showSnackbar(R.string.error_unable_to_edit)
                                } else {
                                    showSnackbar(R.string.text_collection_name_changed)
                                    // reset the title
                                    view?.findViewById<TextView>(R.id.fragment_collection_view_title)?.text = name
                                    // do a hacky change in the duser
                                    SharedData.duserRw(requireContext()) {
                                        val user = it?.user?: return@duserRw
                                        val collections = user.collections?: return@duserRw
                                        val index = collections.indexOfFirst { it.uuid == coll.uuid }
                                        collections[index].name = name
                                        // put back
                                        user.collections = collections
                                    }
                                }

                                loading.dismiss()
                            },
                            onComplete = {
                            }
                    )

            subscriptions.add(sub)
        }

        MaterialDialog.Builder(requireContext())
                .title(R.string.title_collection_name)
                .inputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(R.string.hint_collection_title, 0, false) {
                    _, input ->
                    // send a create request
                    changeName(input.toString())
                }
                .negativeText(R.string.dialog_cancel)
                .cancelable(true)
                .show()
    }

    private fun onSend() {
        val ref = REMOTE_COLLECTIONS_UI + collection?.uuid

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, ref)
        requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_collection)))
    }

    private fun onPostLike(post: DPost) {
        val user = SharedData.duserRo(requireContext())?: return

        if(post.ownerId == user.user._id) {
            showSnackbar(R.string.error_cant_like_own_post)
            return
        }

        // submit an post like action
        val subscription = RxFirebaseAuth.getToken()
                .subscribeOn(Schedulers.io())
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .flatMap {
                    RemoteService.postLike(it, post._id!!)
                            .subscribeOn(Schedulers.io())
                            .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                }
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                            showSnackbar(R.string.error_unable_to_like)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_collection_view, container, false)

        beginLoadAsync(layout)

        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cleanup()
    }
}