package com.asdev.edu

import com.google.gson.GsonBuilder
import java.util.*

/**
 * A standard set of primary material colors that are used for the course backgrounds.
 */
val MD_COURSE_COLORS = arrayOf(
        R.color.md_amber_500,
        R.color.md_green_500,
        R.color.md_indigo_500,
        R.color.md_orange_500,
        R.color.md_pink_500,
        R.color.md_red_500)

/**
 * A convenience random object.
 */
val RANDOM = Random()

val GSON = GsonBuilder().setLenient().create()

/**
 * Request code for Firebase sign in.
 */
const val RC_FB_SIGNIN = 4001

/**
 * Request code for Google Maps Place Picker activity.
 */
const val RC_PLACE_PICKER = 4002

const val RC_IMAGE_ACTIVITY = 4003

/**
 * The file name of the locally cached DUser object.
 */
const val DUSER_FILE = "duser.json"

const val NETWORK_TIMEOUT = 7000L

/**
 * Intent extra key for the DUser object.
 */
const val EXTRA_DUSER = "duser"

/**
 * The name of the locally saved (app settings) preferences.
 */
const val LOCAL_PREFS_NAME = "LocalPrefs"

const val PREF_KEY_HAS_SHOWN_ONB = "has_shown_onb"