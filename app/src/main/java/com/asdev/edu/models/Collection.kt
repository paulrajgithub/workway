/*
 *  NOTICE FOR FILE:
 *  Collection.kt
 *
 *  Copyright (c) 2015-2017. Created by and property of Asdev Software Development, on 7/16/17 10:45 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module edu-backend_main
 */

package com.asdev.edu.models

import java.io.Serializable

/**
 * Represents a collection of posts that a user can save. By default,
 * collections are only visible to the user themselves or a person with the link.
 */
data class DCollection(
        /**
         * The database UUID of the post. Safe to use as a permanent post referencer.
         */
        var uuid: String, /// used to reference the collection
        /**
         * The time of submission of the post, in milliseconds since epoch (System.currentTimeMillis()).
         */
        var submitTime: Long,
        /**
         * The textual name of the collection, as denoted by the creator.
         */
        var name: String,
        /**
         * A list of UUIDS of the posts contained within this collection, guaranteed to be supplied.
         */
        var postRefs: List<String>,
        /**
         * A list of posts contained within this collection, nullable to avoid circular dependencies.
         */
        var posts: List<DPost>?,
        /**
         * The creator/owner of this collection
         */
        var creator: DUser?): Serializable