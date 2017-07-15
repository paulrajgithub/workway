package com.asdev.edu

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.asdev.edu.models.DUser
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // init firebase
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        // check if signed in or not
        if(user == null) {
            // sign in anon
            val signIn = auth.signInAnonymously()

            signIn.addOnSuccessListener {
                // use this auth user for sign in
                val authUser = it.user

                // standard app launch
                // retrieve the local DUser obj
                val duser = loadDuser(authUser)
                launchMain(duser)
            }

            signIn.addOnFailureListener {
                // no sign in at all
                launchMain(null)
            }
        } else {
            val duser = loadDuser(user)
            // standard app launch
            launchMain(duser)
        }
    }

    private fun launchMain(duser: DUser?) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        // put duser extra
        intent.putExtra(EXTRA_DUSER, duser)
        // launch
        finish()
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun loadDuser(user: FirebaseUser): DUser {
        // retrieve the local DUser obj
        var duser = readDiskDuser(applicationContext)

        if(duser == null) {
            val blank = DUser.blank()
            // set to firebase id
            blank.firebaseId = user.uid
            duser = blank
            commitDiskDuser(duser, applicationContext)
            // TODO: notify new user reg
        } else {
            // check that the firebase ids match
            if(duser.firebaseId != user.uid) {
                // blank it and use the new id
                val blank = DUser.blank()
                blank.firebaseId = user.uid
                duser = blank
                commitDiskDuser(duser, applicationContext)
                // TODO: notify new user reg
            }
        }

        return duser
    }

}