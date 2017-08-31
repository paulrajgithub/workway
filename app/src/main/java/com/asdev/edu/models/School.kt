package com.asdev.edu.models

import com.asdev.edu.containsBits

data class DSchool(
        val name: String,
        val placeId: String) {

    companion object {

        fun fromTag(tag: DTag?): DSchool? {
            if (tag == null) {
                return null
            }

            if(!(tag.scope containsBits TAG_SCOPE_SCHOOL) || tag.id !is String)
                return null

            return DSchool(tag.text, tag.id)
        }

    }

    fun toTag() = DTag(name, placeId, TAG_SCOPE_SCHOOL)

}
