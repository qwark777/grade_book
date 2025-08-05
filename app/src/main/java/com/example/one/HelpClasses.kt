package com.example.one

data class TeacherData(
    val fullName: String,
    val workPlace: String,
    val location: String,
    val subject: String,
    val classes: String,
    val username: String,
    val password: String
)

data class StudentData(
    val fullName: String,
    val className: String,
    val school: String
)
data class ClassItem(
    val name: String,
    val studentCount: Int
)

sealed class DropdownItem {
    data class Header(val title: String) : DropdownItem()
    data class Option(val label: String, val type: String) : DropdownItem()
}

data class RatingItem(
    val place: Int,
    val name: String,
    val average: Int
)

data class ConversationPreview(
    val conversation_id: Int,
    val user_id: Int,
    val full_name: String,
    val photo_url: String?,
    val last_message: String,
    val last_time: String,
    val last_sender_id: Int
)
