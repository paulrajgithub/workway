package com.asdev.edu.fragments.main

import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.asdev.edu.R
import com.asdev.edu.REMOTE_COLLECTIONS_UI
import com.asdev.edu.adapters.AdapterCollections
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
 * A fragment for the [MainActivity] which displays the user's collections.
 */
class FragmentCollections: SelectableFragment() {

    private var subscriptions = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // dispose previous subscriptions, and recreate the disposable
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        // inflate the home layout
        val layout = inflater.inflate(R.layout.fragment_collections, container, false)

        // create the behavior subject to receive UI events
        val subject = BehaviorSubject.create<DUIAction<DCollection>>()

        val subscription = subject.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    if(it.type == DUIAction.TYPE_COLLECTION_FULLSCREEN) {
                        onCollectionView(it.payload)
                    } else if(it.type == DUIAction.TYPE_COLLECTION_SHARE) {
                        onCollectionShare(it.payload)
                    } else if(it.type == DUIAction.TYPE_COLLECTION_EDIT) {
                        onCollectionEdit(it.payload)
                    }
                }

        subscriptions.add(subscription)

        // grab the recycler view and bind it to the adapter
        val adapter = AdapterCollections(context!!, subject)
        val recycler = layout.findViewById<RecyclerView>(R.id.fragment_collections_recycler)
        val manager = GridLayoutManager(context, 2)

        // add a special lookup to make sure the header spans 2 grids
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if(position == 0)
                    return 2 // header takes 2 for span
                return 1 // regular items take 1
            }
        }
        recycler.layoutManager = manager

        recycler.adapter = adapter

        // update the balance
        val user = SharedData.duserRo(requireContext())
        layout.findViewById<TextView>(R.id.collections_reward_balance).text = "R${user?.ostUser?.token_balance?.roundToInt()?: 0}"

        // bind search
        layout.findViewById<View>(R.id.fragment_collections_search).setOnClickListener {
            launchSearch(user?.user)
        }

        return layout
    }

    private fun launchSearch(user: DUser?) {
        val target = FragmentSearch()
        target.setType(TYPE_COLLECTION)
        target.setFilter(CollectionFilter(user))

        requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, target)
                .commit()
    }

    private fun onCollectionView(coll: DCollection) {
        val target = FragmentCollectionView()
        target.set(TYPE_STANDARD, coll)

        // transition in to it
        requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .replace(R.id.content, target)
                .commit()
    }

    private fun onCollectionEdit(coll: DCollection) {
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

    private fun onCollectionShare(coll: DCollection) {
        val ref = REMOTE_COLLECTIONS_UI + coll.uuid

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, ref)
        requireActivity().startActivity(Intent.createChooser(intent, getString(R.string.title_share_collection)))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        subscriptions.dispose()
    }

    override fun onSelected() {
    }

    override fun onReselected() {
    }

}