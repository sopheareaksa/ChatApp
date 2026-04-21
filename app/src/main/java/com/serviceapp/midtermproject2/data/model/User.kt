package com.serviceapp.midtermproject2.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val status: String = "Offline",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", "", "", 0L)
}