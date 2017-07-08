package com.asdev.edu.models

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
const val TAG_SCOPE_ALL     = 0b000000
/**
 * Tag scope for school.
 */
const val TAG_SCOPE_SCHOOL  = 0b000001
/**
 * Tag scope for grade.
 */
const val TAG_SCOPE_GRADE   = 0b000010
/**
 * Tag scope for course.
 */
const val TAG_SCOPE_COURSE  = 0b000100
/**
 * Tag scope for type of document.
 */
const val TAG_SCOPE_TYPE    = 0b001000
/**
 * Tag scope for professor.
 */
const val TAG_SCOPE_PROF    = 0b010000

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
 * A class that represents a user post object with an image, owner, and other attrs.
 */
data class DPost(
        /**
         * The mongo ID of this object.
         */
        var _id: Int,
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
         * The UID of the owner of this post.
         */
        var ownerId: String,
        /**
         * The submission time of this post.
         */
        var submitTime: Long,
        /**
         * The visibility of this post.
         */
        var visibility: Int
)