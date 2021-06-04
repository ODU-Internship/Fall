package com.jeevan.fall.data.auth

import com.jeevan.fall.data.auth.datasources.FirebaseAuthDataSource
import com.jeevan.fall.data.auth.models.CreateUser
import javax.inject.Inject

class AuthRepository @Inject constructor(private val dataSource: FirebaseAuthDataSource) {
    suspend fun isUserExists(uid: String): Boolean {
        return dataSource.isUserDataExists(uid)
    }

    suspend fun createUser(uid: String, newUser: CreateUser) {
        return dataSource.createUser(uid, newUser)
    }
}