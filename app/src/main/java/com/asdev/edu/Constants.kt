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
 * A set of vibrant colors that are used for the course backgrounds.
 */
val VIBRANT_COURSE_COLORS = arrayOf(
        R.color.vibrant_green,
        R.color.vibrant_orange,
        R.color.vibrant_pink,
        R.color.vibrant_purple,
        R.color.vibrant_red,
        R.color.vibrant_teal,
        R.color.vibrant_violet,
        R.color.vibrant_blue
)

/**
 * A set of gradient drawable (res IDs) used for placeholders and collection accents.
 */
val GRADIENTS = arrayOf(
        R.drawable.gradient_accent,
        R.drawable.gradient_digital_water,
        R.drawable.gradient_dusk,
        R.drawable.gradient_orange_sky
)

/**
 * The currently selected set of course colors.
 */
val COURSE_COLORS = VIBRANT_COURSE_COLORS

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

const val RC_ADDITIONAL_IMAGE_PICKER = 4004

/**
 * The file name of the locally cached DUser object.
 */
const val DUSER_FILE = "duser.json"

const val QUICK_SAVED_FILE = "qs.json"

/**
 * The amount of time in ms that qualifies as a network timeout.
 */
const val NETWORK_TIMEOUT = 7000L

const val DEFAULT_HOME_FEED_LEN = 20

/**
 * The quality of the JPEG image to export and upload
 * to the servers.
 */
const val EXPORT_JPEG_QUALITY = 90

/**
 * Intent extra key for the DUser object.
 */
const val EXTRA_DUSER = "duser"

/**
 * The name of the locally saved (app settings) preferences.
 */
const val LOCAL_PREFS_NAME = "LocalPrefs"

/**
 * The preference key denoting whether or not the onb has been shown.
 */
const val PREF_KEY_HAS_SHOWN_ONB = "has_shown_onb"

const val REMOTE_BASE_UI = "https://hs.asdev.ca"
const val REMOTE_POST_UI = "$REMOTE_BASE_UI/post/"
const val REMOTE_COLLECTIONS_UI = "$REMOTE_BASE_UI/collection/"