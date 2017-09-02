package com.asdev.edu

import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_image.*
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.PermissionChecker
import android.view.View
import android.widget.LinearLayout
import com.asdev.edu.fragments.img.FragmentImgGallery
import com.asdev.edu.fragments.img.FragmentImgPhoto
import com.asdev.edu.fragments.main.FragmentCollections
import kotlinx.android.synthetic.main.fragment_img_photo.*


class ImageActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: ViewPagerAdapter
    private lateinit var fragmentPhoto: FragmentImgPhoto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val tb = toolbar
        // setup toolbar navigation
        tb.setNavigationIcon(R.drawable.ic_clear_black_24dp)
        tb.setNavigationOnClickListener { onBackPressed() }

        fragmentPhoto = FragmentImgPhoto()

        // setup the view pager
        setupViewPager()
    }

    private fun setupViewPager() {
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        // add in fragments
        pagerAdapter.addFragment(FragmentImgGallery(), this, R.string.tab_gallery)
        pagerAdapter.addFragment(fragmentPhoto, this, R.string.tab_photo)
        val vp = image_content_viewpager
        vp.adapter = pagerAdapter
        tabs.setupWithViewPager(vp)
    }

    override fun onBackPressed() {
        // check if any fragments intercept the back press
        if(!fragmentPhoto.interceptBackPress()) {
            super.onBackPressed()
        }
    }

}

internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItem(position: Int) = mFragmentList[position]

    override fun getCount() = mFragmentList.size

    fun addFragment(fragment: Fragment, context: Context, @StringRes title: Int) = addFragment(fragment, context.getString(title))

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int) = mFragmentTitleList[position]
}