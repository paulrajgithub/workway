package com.asdev.edu

import android.content.Context
import com.asdev.edu.models.DCourse
import com.asdev.edu.models.DUser
import java.io.*

/**
 * Returns whether or not this int contains the given bits.
 */
infix fun Int.containsBits(flag: Int)
        = (this and flag) == flag

fun readDiskDuser(appContext: Context): DUser? {
    // read from duser file
    val file = File(appContext.filesDir, DUSER_FILE)

    if(!file.exists())
        return null

    val body = readFile(file)
    try {
        // parse the body as a duser obj
        return GSON.fromJson(body, DUser::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun commitDiskDuser(duser: DUser, appContext: Context) {
    val file = File(appContext.filesDir, DUSER_FILE)

    // convert duser to json
    val body = GSON.toJson(duser)
    // write out
    writeFile(body, file)
}

/**
 * Writes the given content to the given file.
 */
fun writeFile(content: String, file: File) {
    val ous = BufferedWriter(FileWriter(file, false))
    ous.write(content)
    ous.close()
}

/**
 * Reads the given file and returns the contents as a String.
 */
fun readFile(file: File): String {
    val ins = BufferedReader(FileReader(file))

    val builder = StringBuilder()

    var line: String?
    do {
        line = ins.readLine()
        if(line != null) {
            builder.append(line)
            builder.append("\n")
        }
    } while(line != null)

    ins.close()

    return builder.toString().trim()
}

/**
 * Returns all the courses in order of priority, determined
 * by whether or not it is starred.
 */
fun getCoursesInPriority(duser: DUser?): List<DCourse> {
    if(duser == null) {
        for(c in DCourse.values())
            c.isStarred = false
        return DCourse.values().asList()
    }

    val first = duser.starredCourses
    val distinct = DCourse.values().filterNot { first.contains(it) }

    // reset starred global states
    for(c in DCourse.values())
        c.isStarred = false
    for(c in first)
        c.isStarred = true

    return first + distinct
}