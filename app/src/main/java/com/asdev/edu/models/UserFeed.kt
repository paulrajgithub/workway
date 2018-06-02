/*
 *  NOTICE FOR FILE:
 *  UserFeed.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 19/05/18 8:06 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module edu-backend_main
 */

package com.asdev.edu.models

data class UserFeed(
        val fromFollowers: List<DPost>,
        val fromSchool: List<DPost>)