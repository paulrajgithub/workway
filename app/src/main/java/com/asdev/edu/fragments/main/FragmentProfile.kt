package com.asdev.edu.fragments.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.R
import com.asdev.edu.REMOTE_POST_UI
import com.asdev.edu.adapters.AdapterUserPosts
import com.asdev.edu.dialogs.DialogChooseCollection
import com.asdev.edu.models.*
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.roundToInt

/**
 * A fragment for the [com.asdev.edu.MainActivity] which displays the user's profile.
 */
class FragmentProfile: SelectableFragment() {

    private lateinit var adapter: AdapterUserPosts
    private val actionHandler = BehaviorSubject.create<DUIAction<DPost>>()
    private var actionSub: Disposable? = null

    private var subscriptions = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        val layout = inflater.inflate(R.layout.fragment_profile, container, false)

        if(actionSub == null) {
            actionSub = actionHandler.subscribeBy(
                    onNext = { action ->
                        // no need to receive post like events, as own post liking is not a thing
                        when (action.type) {
                            DUIAction.TYPE_POST_FULLSCREEN -> {
                                // launch full screen fragment
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

                                return@subscribeBy
                            }

                            DUIAction.TYPE_POST_SEND -> {
                                // create a share intent
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, REMOTE_POST_UI + action.payload._id)
                                requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_post)))

                                return@subscribeBy
                            }

                            DUIAction.TYPE_POST_SAVE -> {
                                onPostSave(action.payload)
                                return@subscribeBy
                            }
                        }
                    }
            )
        }

        // bind the recycler view to the adapter
        adapter = AdapterUserPosts(inflater.context, actionHandler)
        val recycler = layout.findViewById<RecyclerView>(R.id.fragment_profile_recycler)
        // set layout
        recycler.layoutManager = GridLayoutManager(context, 2)
        recycler.adapter = adapter

        val user = SharedData.duserRo(context!!)

        // update text
        layout.findViewById<TextView>(R.id.fragment_profile_name).text = user?.user?.name?: getString(R.string.text_user_not_logged_in)
        layout.findViewById<TextView>(R.id.fragment_profile_subtitle).text = user?.user?.schoolName?: getString(R.string.text_user_do_sign_in)

        if(user?.ostUser != null) {
            layout.findViewById<TextView>(R.id.chip_balance).text = "R${user.ostUser.token_balance.roundToInt()}"
        }

        return layout
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

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.dispose()
        subscriptions = CompositeDisposable()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        println("On attach")
    }

    override fun onDetach() {
        super.onDetach()

        println("On deattach")
    }

    override fun onSelected() {
    }

    override fun onReselected() {
    }

    // shorter lifecycle of subscriptions
    override fun onUnselected() {

    }

}