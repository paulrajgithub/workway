package com.asdev.edu.models

import com.asdev.edu.containsBits
import java.io.Serializable

/**
 * Created by Asdev on 07/04/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of HomeworkShare
 * under the package com.asdev.edu
 */

/**
 * Represents a non-standard (custom) tag id.
 */
const val TAG_ID_NONSTANDARD = -1

///// TAG SCOPES /////

/**
 * Represents a global tag scope.
 */
const val TAG_SCOPE_ALL      = 0b000000
/**
 * Tag scope for school.
 */
const val TAG_SCOPE_SCHOOL   = 0b000001
/**
 * Tag scope for grade.
 */
const val TAG_SCOPE_GRADE    = 0b000010
/**
 * Tag scope for course.
 */
const val TAG_SCOPE_COURSE   = 0b000100
/**
 * Tag scope for type of document.
 */
const val TAG_SCOPE_TYPE     = 0b001000
/**
 * Tag scope for professor.
 */
const val TAG_SCOPE_PROF     = 0b010000

/**
 * A class that represents an associative textual tag.
 */
data class DTag(val text: String, val id: Any = TAG_ID_NONSTANDARD, val scope: Int = TAG_SCOPE_ALL) {

    companion object {

        /**
         * Creates a new tag for a professor with the
         * given name.
         */
        fun forProfessor(name: String) =
                DTag(name, name, TAG_SCOPE_PROF)

    }

}

/**
 * Visibility modifier stating that everyone can see this post.
 */
const val VISIBILITY_PUBLIC = 0
/**
 * Visibility modifier stating that only the users following can see this post.
 */
const val VISIBILITY_FOLLOWING = 1
/**
 * Visibility modifier stating that only the user can see their post.
 */
const val VISIBILITY_PRIVATE = 2

data class DUser(
        /**
         * The mongo ID of this user.
         */
        var _id: String?,
        /**
         * The firebase id of this user.
         */
        var firebaseId: String,
        /**
         * The URL to the profile picture of this user.
         */
        var profilePicRef: String?,
        /**
         * The references to the posts of this user.
         */
        var postRefs: List<String>,
        /**
         * The posts of this user, nullable to avoid circular dependencies.
         */
        var posts: List<DPost>?,
        /**
         * The starred courses for this user. Usually part of the user's timetable.
         */
        var starredCourses: List<DCourse>,
        /**
         * Users that follow this user.
         */
        var followers: List<String>,
        /**
         * Users that this user follows.
         */
        var following: List<String>,
        /**
         * The grade of this user
         */
        var grade: DGrade,
        /**
         * The place id of the school of the user
         */
        var schoolPlaceId: String,
        /**
         * The name of the school of the user.
         */
        var schoolName: String): Serializable {

    companion object {

        /**
         * Creates a new blank DUser.
         */
        fun blank() =
                DUser(
                        _id = null,
                        firebaseId = "",
                        profilePicRef = null,
                        postRefs = listOf(),
                        posts = null,
                        // default courses
                        starredCourses = listOf(
                                DCourse.MATH,
                                DCourse.ENGLISH,
                                DCourse.CHEMISTRY,
                                DCourse.BIOLOGY,
                                DCourse.PHYSICS
                        ),
                        followers = listOf(),
                        following = listOf(),
                        grade = DGrade.GRADE_9,
                        schoolPlaceId = "",
                        schoolName = ""
                )

    }

}

/**
 * A class that represents a user post object with an image, owner, and other attrs.
 */
data class DPost(
        /**
         * The mongo ID of this object.
         */
        var _id: String?,
        /**
         * The textual title of this post.
         */
        var title: String,
        /**
         * The URL to the image containing the post.
         */
        var ref: String,
        /**
         * A list of tags associated with this post.
         */
        var tags: List<DTag>,
        /**
         * The id of the owner of this post.
         */
        var ownerId: String,
        /**
         * The owner of this post, nullable to avoid circular dependencies.
         */
        var owner: DUser?,
        /**
         * The submission time of this post.
         */
        var submitTime: Long,
        /**
         * The visibility of this post.
         */
        var visibility: Int) {

    /**
     * Attempts to find the course of this post by taking the first tag
     * that contains the scope of TAG_SCOPE_COURSE and taking the textual value.
     */
    fun resolveCourse(): DCourse? =
        DCourse.fromTag(tags.find { it.scope containsBits TAG_SCOPE_COURSE })

    /**
     * Attempts to find the course of this post by taking the first tag
     * that contains the scope of TAG_SCOPE_PROF and taking the textual value.
     */
    fun resolveProfessor() =
        tags.find { it.scope containsBits TAG_SCOPE_PROF }?.text

    /**
     * Attempts to find the course of this post by taking the first tag
     * that contains the scope of TAG_SCOPE_TYPE.
     */
    fun resolveDocType(): DDocType? =
        DDocType.fromTag(tags.find { it.scope containsBits TAG_SCOPE_TYPE })

    /**
     * Attempts to find the grade level of this post by taking the first tag
     * that contains the scope of TAG_SCOPE_GRADE.
     */
    fun resolveGrade(): DGrade? =
            DGrade.fromTag(tags.find { it.scope containsBits TAG_SCOPE_GRADE })

    /**
     * Attempts to find the school of this post by taking the first tag
     * that contains the scope of TAG_SCOPE_SCHOOL and taking the textual value.
     */
    fun resolveSchool(): DSchool? =
            DSchool.fromTag(tags.find { it.scope containsBits TAG_SCOPE_SCHOOL })
}

/**
 * Represents an actionable ui event. Useful with handlers and subjects.
 */
data class DUIAction<out T>(val type: Int, val payload: T) {

    companion object {

        val TYPE_POST_FULLSCREEN = 1
        val TYPE_POST_SAVE = 2
        val TYPE_POST_SEND = 3
        val TYPE_POST_COLLECTION = 4

    }
}