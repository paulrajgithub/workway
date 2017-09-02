package com.asdev.edu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.asdev.edu.fragments.onb.*
import com.asdev.edu.models.DGrade
import com.asdev.edu.models.DSchool
import com.asdev.edu.models.DUser
import com.asdev.edu.services.Localization
import com.asdev.edu.services.RemoteResponse
import com.asdev.edu.services.RemoteService
import com.asdev.edu.services.RxFirebaseAuth
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_on_boarding.*
import kotlinx.android.synthetic.main.fragment_onb_profile.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class OnBoardingActivity : AppCompatActivity() {

    /**
     * Whether or not the user can navigate backwards.
     */
    private var isBackAvailable = true

    /**
     * The user selected place for their place of institution.
     */
    private var pickedPlace: Place? = null

    /**
     * A list of RxJava subscriptions that will be disposed when the activity is paused.
     */
    private var subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        // check if previously shown main content
        if (getSharedPreferences(LOCAL_PREFS_NAME, Context.MODE_PRIVATE).getBoolean(PREF_KEY_HAS_SHOWN_ONB, false)) {
            loadFragmentProfile(false)
        } else {
            loadFragmentContent(false)
        }
    }

    //// FRAGMENT AND STATE RECEIVERS ////

    private fun loadFragmentContent(addToBackstack: Boolean) {
        // load in the main content fragment
        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment, FragmentOnbContent())
        if (addToBackstack)
            transaction.addToBackStack(FRAGMENT_ONB_CONTENT)
        transaction.commit()
    }

    private fun loadFragmentProfile(addToBackstack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment, FragmentOnbProfile())
        if (addToBackstack)
            transaction.addToBackStack(FRAGMENT_ONB_PROFILE)
        transaction.commit()
    }

    private fun loadFragmentLoading() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment, FragmentOnbLoading())
                .addToBackStack(FRAGMENT_ONB_LOADING)
                .commit()
    }

    private fun handleSignIn() {
        // we have an auth response, finalize the registration by showing loading,
        // saving locally the data
        // and sending to server

        // make sure we dont go back while loading
        isBackAvailable = false

        val school = pickedPlace
        val grade = DGrade.values()[onboarding_spinner_grade.selectedItemPosition] // take the selected DGrade by index

        // make sure the place was picked
        if (school == null) {
            isBackAvailable = true
            showSnackbar(R.string.error_no_school_picked)
            return
        }

        // we have gathered all required info, proceed with the registration
        // show loading fragment
        loadFragmentLoading()

        // launch the sign in chain
        val sub = RxFirebaseAuth.getToken()
                .subscribeOn(Schedulers.io())
                .flatMap { token ->
                    RemoteService.userRegister(
                            authToken = token,
                            grade = grade,
                            school = DSchool(school.name.toString(), school.id),
                            profilePicRef = null,
                            starredCourses = DUser.blank().starredCourses
                    )
                }.timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = this::handleSignInFail,
                        onNext = this::handleSignInSuccess
                )

        subscriptions.add(sub)
    }

    private fun handleSignInSuccess(response: RemoteResponse<DUser>) {
        if (response.error != null) {
            // we had some error
            val msg = Localization.getResponseMsg(response.error)
            isBackAvailable = true
            showSnackbar(msg)
            supportFragmentManager.popBackStack()
            return
        }

        if (response.payload != null) {
            // mark onb completed
            val prefs = getSharedPreferences(LOCAL_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_KEY_HAS_SHOWN_ONB, true).apply()

            // we should have our payload
            // cache response and continue
            commitDiskDuser(response.payload, applicationContext)
            launchMain(response.payload)
        }
    }

    private fun handleSignInFail(t: Throwable) {
        t.printStackTrace()

        if (t is TimeoutException) {
            // timed out
            showSnackbar(R.string.resp_error_request_timeout)
        } else {
            showSnackbar(R.string.resp_error_internal)
        }

        // reset back to the previous profile state
        isBackAvailable = true
        supportFragmentManager.popBackStack()
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

    //// ACTIVITY LIFECYCLE RECEIVERS ////

    override fun onResume() {
        val fm = supportFragmentManager

        // if in loading state, reset
        if (fm.backStackEntryCount > 0 && fm.getBackStackEntryAt(fm.backStackEntryCount - 1).name == FRAGMENT_ONB_LOADING) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            loadFragmentContent(false)
            isBackAvailable = true
        }

        super.onResume()
    }

    override fun onPause() {
        // destroy running subscriptions
        subscriptions.dispose()
        subscriptions = CompositeDisposable()

        super.onPause()
    }

    override fun onBackPressed() {
        if (!isBackAvailable)
            return

        super.onBackPressed()
    }

    ///// CONTENT RECEIVERS /////

    fun actionNextContent(@Suppress("UNUSED_PARAMETER") v: View?) {
        // go to the profile section
        loadFragmentProfile(true)
    }

    fun actionSkipContent(@Suppress("UNUSED_PARAMETER") v: View?) {
        // same action as next
        actionNextContent(v)
    }

    ///// PROFILE RECEIVERS /////

    fun actionNextProfile(@Suppress("UNUSED_PARAMETER") v: View?) {
        // verify the fields are entered
        if (pickedPlace == null) {
            showSnackbar(R.string.error_no_school_picked)
            return
        }
        // assume the spinner was changed to the desired value

        // launch the sign in intent, when that is completed, mark as completed
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(true)
                .setLogo(R.mipmap.ic_launcher)
                .setProviders(listOf(
                        AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(), // Google accounts
                        AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(), // Facebook accounts
                        AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(), // Twitter accounts
                        AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build() // Email accounts
                )).setAllowNewEmailAccounts(true)
                .build()

        startActivityForResult(intent, RC_FB_SIGNIN)
    }

    fun actionSkipProfile(@Suppress("UNUSED_PARAMETER") v: View?) {
        // finish the activity, mark as completed
        // mark onb completed
        val prefs = getSharedPreferences(LOCAL_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_KEY_HAS_SHOWN_ONB, true).apply()

        launchMain(null)
    }

    fun actionPickSchool(@Suppress("UNUSED_PARAMETER") v: View?) {
        val intent = PlacePicker.IntentBuilder().build(this)
        startActivityForResult(intent, RC_PLACE_PICKER)
    }

    //// UTIL / EXTRA FUNCS ////

    private fun showSnackbar(@StringRes msg: Int) {
        Snackbar.make(onboarding_fragment, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        isBackAvailable = true

        if (requestCode == RC_FB_SIGNIN) {
            val response = IdpResponse.fromResultIntent(data) ?: return

            if (resultCode == RESULT_OK) {
                // got a successful auth response
                handleSignIn()
            } else {
                // error, read code
                if (response.errorCode == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.error_no_network)
                }
            }
        } else if (requestCode == RC_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                // check the place type to be either a school or university
                if (place.placeTypes.indexOfFirst { it == Place.TYPE_SCHOOL || it == Place.TYPE_UNIVERSITY } == -1) {
                    showSnackbar(R.string.error_not_school)
                    return
                }

                // is a valid location, save it
                pickedPlace = place
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
