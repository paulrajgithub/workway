package com.asdev.edu.models

import com.asdev.edu.containsBits

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
 * Tag scope for school google maps place id.
 */
const val TAG_SCOPE_PLACE_ID = 0b100000

/**
 * A class that represents an associative textual tag.
 */
data class DTag(val text: String, val id: Any = TAG_ID_NONSTANDARD, val scope: Int = TAG_SCOPE_ALL)

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

/**
 * Represents the id of a post.
 */
typealias PostId = Int
/**
 * Represents the id of a user.
 */
typealias UserId = String

data class DUser(
        /**
         * The mongo ID of this user.
         */
        var _id: Int,
        /**
         * The firebase id of this user.
         */
        var uid: UserId,
        /**
         * The URL to the profile picture of this user.
         */
        var profilePicRef: String?,
        /**
         * The references to the posts of this user.
         */
        var postRefs: List<PostId>,
        /**
         * The posts of this user, nullable to avoid circular dependencies.
         */
        var posts: List<DPost>?,
        /**
         * The starred courses for this user. Usually part of the user's timetable.
         */
        var starredCourses: List<DCourse>)

/**
 * A class that represents a user post object with an image, owner, and other attrs.
 */
data class DPost(
        /**
         * The mongo ID of this object.
         */
        var _id: PostId,
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
        var ownerId: UserId,
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

}

/**
 * Represents an actionable ui event. Useful with handlers and subjects.
 */
data class DUIAction<out T>(val type: Int, val payload: T) {

    companion object {

        val TYPE_POST_FULLSCREEN = 1
        val TYPE_POST_SAVE = 2
        val TYPE_POST_SEND = 3

    }
}