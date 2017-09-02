package com.asdev.edu

import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.asdev.edu.fragments.main.FragmentCollections
import com.asdev.edu.fragments.main.FragmentCreate
import com.asdev.edu.fragments.main.FragmentHome
import com.asdev.edu.fragments.main.FragmentProfile
import com.asdev.edu.models.SelectableFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fragments = arrayOf(FragmentHome(), FragmentCollections(), FragmentProfile(), FragmentCreate())

    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: SelectableFragment = when (item.itemId) {
            R.id.navigation_home -> {
                fragments[0]
            }
            R.id.navigation_collections -> {
                fragments[1]
            }
            R.id.navigation_profile -> {
                fragments[2]
            }
            R.id.navigation_create -> {
                fragments[3]
            }
            else -> fragments[0]
        }

        // check for reselection
        if(item.itemId == navigation.selectedItemId) {
            fragment.onReselected()
            return@OnNavigationItemSelectedListener false
        }

        fragment.onSelected()

        // do a transaction of the new fragment
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init the network client
        AndroidNetworking.initialize(applicationContext)

        navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener)

        // do the initial fragment
        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragments[0])
                .commit()

        val menuView = navigation.getChildAt(0) as BottomNavigationMenuView

        // disable shifting
        for (i in 0 until menuView.childCount) {
            val child = menuView.getChildAt(i) as BottomNavigationItemView
            child.setShiftingMode(false)
            child.setChecked(false)
        }
    }

}
