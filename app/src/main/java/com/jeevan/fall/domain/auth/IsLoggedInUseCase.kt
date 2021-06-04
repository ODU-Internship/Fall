package com.jeevan.fall.domain.auth

import com.google.firebase.auth.FirebaseAuth
import com.jeevan.fall.data.auth.AuthRepository
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, Boolean>(dispatcher) {
    override suspend fun execute(parameters: Unit): Boolean =
        auth.currentUser?.let {
            return authRepository.isUserExists(it.uid)
        } ?: false
}