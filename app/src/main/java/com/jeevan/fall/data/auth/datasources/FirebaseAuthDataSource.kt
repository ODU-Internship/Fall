package com.jeevan.fall.data.auth.datasources;

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jeevan.fall.data.auth.models.CreateUser
import com.jeevan.fall.util.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    suspend fun isUserDataExists(uid: String): Boolean {
        if (uid.isEmpty()) {
            return false
        }
        val snapshot = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
        return snapshot.exists()
    }

    suspend fun createUser(uid: String, newUser: CreateUser) {
        if (uid.isEmpty()) {
            return
        }
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(newUser)
            .await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}

