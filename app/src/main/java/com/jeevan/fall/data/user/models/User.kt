package com.jeevan.fall.data.user.models

import com.google.firebase.auth.FirebaseUser

data class User(
    val firebaseUser: FirebaseUser?,
    val name: String,
    val age: Int,
    val address: String,
) : FirebaseUserInfo(firebaseUser)