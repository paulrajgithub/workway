package com.asdev.edu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.asdev.edu.models.UserBundle
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        FirebaseApp.initializeApp(this)

        // check the local prefs for on boarding sign
        val prefs = getSharedPreferences(LOCAL_PREFS_NAME, Context.MODE_PRIVATE)
        if(!prefs.getBoolean(PREF_KEY_HAS_SHOWN_ONB, false)) {
            // launch on boarding activity and remove this activity
            launchOnb()
            return
        }

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val duser = readJsonFile<UserBundle>(applicationContext, DUSER_FILE)

        // verify the authentication state
        if(user != null && duser?.user != null && duser.user.firebaseId == user.uid) {
            launchMain(duser)
        } else if(user == null && duser == null){
            // if no auth and no duser, that means the ONB was skipped
            // launch with no context
            launchMain(null)
        } else {
            // mismatch between auth user and duser
            // sign out fb user, delete disk duser, and reset state
            auth.signOut()
            launchOnb()
        }
    }

    private fun launchOnb() {
        finish()
        startActivity(Intent(this, OnBoardingActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun launchMain(duser: UserBundle?) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        // put duser extra
        intent.putExtra(EXTRA_DUSER, duser)
        // launch
        finish()
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}