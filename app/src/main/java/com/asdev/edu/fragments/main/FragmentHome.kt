package com.asdev.edu.fragments.main

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.NestedScrollView
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.NETWORK_TIMEOUT
import com.asdev.edu.R
import com.asdev.edu.REMOTE_POST_UI
import com.asdev.edu.adapters.AdapterPosts
import com.asdev.edu.dialogs.DialogChooseCollection
import com.asdev.edu.dialogs.DialogLike
import com.asdev.edu.getCoursesInPriority
import com.asdev.edu.models.*
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import com.asdev.edu.views.VHCourseSelector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val COURSE_SHOW_COUNT_DEFAULT = 8

/**
 * A fragment for the [com.asdev.edu.MainActivity] which displays the home landing content.
 */
class FragmentHome: SelectableFragment() {

    private val behaviorSubject = BehaviorSubject.create<DUIAction<DPost>>()

    private val followersPostAdapter = AdapterPosts(mutableListOf(), behaviorSubject)
    private val schoolPostAdapter = AdapterPosts(mutableListOf(), behaviorSubject)

    private var subscriptions = CompositeDisposable()
    private var behaviorSub: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // bring the header (search UI, etc) to the front
        (view.findViewById(R.id.header) as LinearLayout).bringToFront()

        // add course items
        val courseGrid = view.findViewById(R.id.home_grid_courses) as GridLayout

        val user = SharedData.duserRo(context!!)

        // show at least 8 courses or at least the total number of starred courses, with the starred courses being first priority
        val courses = getCoursesInPriority(user?.user)

        val courseCallback: (DCourse) -> Unit = {
            // call search
            launchSearch(CourseFilter(it, requireContext()))
        }

        for(i in 0 until COURSE_SHOW_COUNT_DEFAULT) {
            VHCourseSelector.inflate(courses[i], courseGrid, courseCallback)
        }

        // bind view more button
        view.findViewById<Button>(R.id.fragment_home_view_more_courses).setOnClickListener {
            for(i in COURSE_SHOW_COUNT_DEFAULT until courses.size) {
                // inflate remaining
                VHCourseSelector.inflate(courses[i], courseGrid, courseCallback)
            }

            // visibility gone of view
            it.visibility = View.GONE
        }

        val followersRecycler = view.findViewById<RecyclerView>(R.id.fragment_home_from_followers)
        followersRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        followersRecycler.adapter = followersPostAdapter

        if(followersPostAdapter.itemCount != 0) {
            view?.findViewById<TextView>(R.id.fragment_home_from_followers_header)?.visibility = View.VISIBLE
        }

        val schoolRecycler = view.findViewById<RecyclerView>(R.id.fragment_home_from_school)
        schoolRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        schoolRecycler.adapter = schoolPostAdapter

        if(schoolPostAdapter.itemCount != 0) {
            view?.findViewById<TextView>(R.id.fragment_home_from_school_header)?.visibility = View.VISIBLE
        }

        // get the swipe refresh
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.fragment_home_swipe_refresh)
        swipeRefresh.setProgressViewOffset(false, 0, resources.getDimensionPixelSize(R.dimen.swipe_refresh_end))
        swipeRefresh.setColorSchemeResources(R.color.vibrant_red)
        swipeRefresh.setOnRefreshListener {
            // call a reload feed, dismiss the swipe refresh
            reloadFeed()
        }

        view.findViewById<TextView>(R.id.home_reward_balance).text = "R${user?.ostUser?.token_balance?.roundToInt()?: 0}"

        // bind search bar
        view.findViewById<TextView>(R.id.fragment_home_search_text).setOnClickListener {
            launchSearch(null)
        }

        user?.user?.let {
            u ->
            view.findViewById<View>(R.id.fragment_home_search_by_user).setOnClickListener {
                launchSearch(UserFilter(u))
            }
        }


        user?.user?.let {
            u ->
            view.findViewById<View>(R.id.fragment_home_search_by_school).setOnClickListener {
                launchSearch(SchoolFilter(u.schoolPlaceId, u.schoolName))
            }
        }


        view.findViewById<View>(R.id.fragment_home_search_by_saved).setOnClickListener {

        }

        return view
    }

    private fun launchSearch(filter: SearchFilter?) {
        // launch search fragment
        val target = FragmentSearch()
        target.setFilter(filter)

        requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, target)
                .commit()
    }

    fun reloadFeed() {
        // trash earlier subscriptions
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        if(behaviorSub == null) {
            behaviorSub = behaviorSubject.subscribeBy(
                    onNext = { action ->
                        when {
                            action.type == DUIAction.TYPE_POST_LIKE -> {
                                onPostLike(action.payload)
                            }
                            action.type == DUIAction.TYPE_POST_FULLSCREEN -> {
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
                            }
                            action.type == DUIAction.TYPE_POST_SEND -> {
                                // create a share intent
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, REMOTE_POST_UI + action.payload._id)
                                requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_post)))
                            }

                            action.type == DUIAction.TYPE_POST_SAVE -> {
                                onPostSave(action.payload)
                            }
                        }
                    }
            )
        }

        // subscriptions.add(sub)

        val subscription = RxFirebaseAuth.getToken()
                .subscribeOn(Schedulers.io())
                .flatMap {
                    RemoteService.feed(it).subscribeOn(Schedulers.io()).timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                            // dismiss swipe refresh if action
                            val swipeRefresh = view?.findViewById<SwipeRefreshLayout>(R.id.fragment_home_swipe_refresh)
                            swipeRefresh?.isRefreshing = false
                        },
                        onNext = {
                            if(it.error != null && it.payload == null) {

                            } else if(it.payload != null) {
                                if(it.payload.fromFollowers.isNotEmpty()) {
                                    view?.findViewById<TextView>(R.id.fragment_home_from_followers_header)?.visibility = View.VISIBLE
                                    followersPostAdapter.setItems(it.payload.fromFollowers.toMutableList())
                                }

                                if(it.payload.fromSchool.isNotEmpty()) {
                                    view?.findViewById<TextView>(R.id.fragment_home_from_school_header)?.visibility = View.VISIBLE
                                    schoolPostAdapter.setItems(it.payload.fromSchool.toMutableList())
                                }
                            }
                        },
                        onComplete = {
                            // dismiss swipe refresh if action
                            val swipeRefresh = view?.findViewById<SwipeRefreshLayout>(R.id.fragment_home_swipe_refresh)
                            swipeRefresh?.isRefreshing = false
                        }
                )

        subscriptions.add(subscription)

        // invalidate the duser
        SharedData.invalidateDuser(context!!) {
            // update the balance
            view?.findViewById<TextView>(R.id.home_reward_balance)?.text = "R${it.ostUser.token_balance.roundToInt()}"
        }
    }

    private fun onPostLike(post: DPost) {
        val user = SharedData.duserRo(requireContext())

        if(user == null) {
            showSnackbar(R.string.error_must_be_signed_in)
            return
        }

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                            showSnackbar(R.string.error_unable_to_like)
                        },
                        onNext = {
                            Log.d("OSTTransaction", "The following OST action happened: $it")

                            // call invalidate, and reload feed
                            SharedData.invalidateDuser(requireContext())
                            reloadFeed()

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
                    // enforce proper order
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
                                reloadFeed()
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
                                    reloadFeed()
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

    override fun onSelected() {
    }

    override fun onReselected() {
        if(!isVisible) {
            // pop the back stack
            requireActivity()
                    .supportFragmentManager
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            // scroll the layout to the top
            view?.findViewById<NestedScrollView>(R.id.home_content)?.fullScroll(View.FOCUS_UP)
        }
    }
}

fun Fragment.showSnackbar(@StringRes msg: Int) {
    showSnackbar(getString(msg))
}

fun Fragment.showSnackbar(msg: String) {
    Snackbar.make(view!!, msg, Snackbar.LENGTH_LONG).show()
}