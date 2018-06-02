/*
 *  NOTICE FOR FILE:
 *  UserBundle.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 24/05/18 6:14 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module edu-backend_main
 */

package com.asdev.edu.models

import com.asdev.ost.sdk.models.OSTUser
import java.io.Serializable

data class UserBundle(val user: DUser, val ostUser: OSTUser): Serializable