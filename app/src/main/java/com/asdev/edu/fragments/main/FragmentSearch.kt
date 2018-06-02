package com.asdev.edu.fragments.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.algolia.instantsearch.helpers.InstantSearch
import com.algolia.instantsearch.helpers.Searcher
import com.algolia.instantsearch.ui.views.Hits
import com.asdev.edu.R
import com.asdev.edu.models.*
import com.google.gson.GsonBuilder
import kotlin.math.roundToInt

const val TYPE_POST = 0
const val TYPE_COLLECTION = 1

class FragmentSearch: Fragment() {

    private val gson = GsonBuilder().create()

    private var filter: SearchFilter? = null
    private var type = TYPE_POST

    fun setFilter(filter: SearchFilter?) {
        this.filter = filter
    }

    fun setType(type: Int) {
        this.type = type
    }

    private lateinit var searcher: Searcher

    override fun onDestroy() {
        super.onDestroy()

        searcher.destroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(if(type == TYPE_COLLECTION) R.layout.fragment_search_collections else R.layout.fragment_search, container, false)

        // draw focus to search input
        val searchInput = layout.findViewById<EditText>(R.id.fragment_search_input)
        searchInput.requestFocus()

        // change hint as well
        if(type == TYPE_COLLECTION) {
            searchInput.setHint(R.string.text_search_collections)
        }

        // force keyboard up
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        // bind the balance to avoid UI changes
        val user = SharedData.duserRo(requireContext())
        val balance = "R${user?.ostUser?.token_balance?.roundToInt()?: 0}"
        layout.findViewById<TextView>(R.id.fragment_search_balance).text = balance

        val hits = layout.findViewById<Hits>(R.id.fragment_search_hits)
        // create the searcher
        searcher = Searcher.create(getString(R.string.algolia_app_id), getString(R.string.algolia_api_key), getString(if(type == TYPE_COLLECTION) R.string.algolia_collections_index else R.string.algolia_posts_index))
        val helper = InstantSearch(hits, searcher)


        filter?.let {
            it.applyToSearcher(searcher)

            if(type == TYPE_POST) {
                val filterDesc = layout.findViewById<TextView>(R.id.fragment_search_filters)
                // apply to filter description
                val text = getString(R.string.text_filters_prefix) + " " + it.describe(requireContext())
                filterDesc.text = text
            }
        }

        hits.setOnItemClickListener { _, position, _ ->
            val json = hits.get(position)
            json.remove("objectID")
            json.remove("_highlightResult")

            if(type == TYPE_POST) {
                val post = gson.fromJson(json.toString(), DPost::class.java)

                launchPostFragment(post)
            } else if(type == TYPE_COLLECTION) {
                val coll = gson.fromJson(json.toString(), DCollection::class.java)

                onCollectionView(coll)
            }
        }

        helper.search()

        // bind an on text changed listener
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                helper.search(s?.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        return layout
    }

    private fun onCollectionView(coll: DCollection) {
        hideKeyboard()

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

    private fun launchPostFragment(post: DPost) {
        hideKeyboard()

        val target = FragmentPost()
        target.setToPost(post)

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

    private fun hideKeyboard() {
        val searchInput = view?.findViewById<EditText>(R.id.fragment_search_input)
        searchInput?.clearFocus()

        // hide keyboard
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput?.windowToken, 0)
    }

}

interface SearchFilter {

    fun applyToSearcher(searcher: Searcher)

    fun describe(context: Context): String
}

class CourseFilter(val course: DCourse, context: Context): SearchFilter {

    private val tag = course.toTag(context)

    override fun applyToSearcher(searcher: Searcher) {
        searcher.addFacetRefinement("tags.id", tag.id.toString())
    }

    override fun describe(context: Context): String {
        return tag.text
    }

}

class CollectionFilter(val user: DUser?): SearchFilter{
    override fun applyToSearcher(searcher: Searcher) {
        user?._id?.let {
            searcher.addFacetRefinement("creator._id", it)
        }
    }

    override fun describe(context: Context): String {
        return context.resources.getString(R.string.title_collections)
    }

}

class SchoolFilter(val placeId: String, val placeName: String): SearchFilter {

    override fun applyToSearcher(searcher: Searcher) {
        searcher.addFacetRefinement("tags.id", placeId)
    }

    override fun describe(context: Context) = placeName

}

class UserFilter(val user: DUser): SearchFilter  {

    override fun applyToSearcher(searcher: Searcher) {
        searcher.addFacetRefinement("ownerId", user._id!!)
    }

    override fun describe(context: Context) = user.name

}