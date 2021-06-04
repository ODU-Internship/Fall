package com.jeevan.fall.data.user.datasources

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jeevan.fall.data.user.models.User
import com.jeevan.fall.util.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private fun getObservableFirebaseUser(): Flow<FirebaseUser> {
        return channelFlow {
            val authStateListener: ((FirebaseAuth) -> Unit) = { auth ->
                if (isActive) {
                    auth.currentUser.let { channel.offer(it!!) }
                }
            }
            auth.addAuthStateListener(authStateListener)
            awaitClose {
                auth.removeAuthStateListener(authStateListener)
            }
        }
    }

    fun getUserInfo(): Flow<User> {
        return getObservableFirebaseUser().map {
            val user = firestore
                .collection(USERS_COLLECTION)
                .document(it.uid)
                .get()
                .await()
            if (!user.exists()) {
                throw UnsupportedOperationException()
            }

            User(
                it,
                user[NAME_FIELD] as String,
                user[AGE_FIELD] as Int,
                user[ADDRESS_FIELD] as String,
            )
        }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val NAME_FIELD = "name"
        private const val AGE_FIELD = "age"
        private const val ADDRESS_FIELD = "address"
    }


}