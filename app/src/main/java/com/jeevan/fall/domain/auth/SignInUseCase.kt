package com.jeevan.fall.domain.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.jeevan.fall.data.auth.AuthRepository
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.UseCase
import com.jeevan.fall.util.await
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * If its a new account, it returns false,
 * else it return true.
 */
class SignInUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<AuthCredential, Boolean>(dispatcher) {
    override suspend fun execute(parameters: AuthCredential): Boolean {
        val result = auth.signInWithCredential(parameters).await()
        if (result.additionalUserInfo?.isNewUser == true) {
            return false
        }
        // maybe double bang for uid or not, but it should kinda
        // throw an error if it doesn't login so i guess i am good.
        return authRepository.isUserExists(result.user?.uid!!)
    }
}