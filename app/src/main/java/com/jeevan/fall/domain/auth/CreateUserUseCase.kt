package com.jeevan.fall.domain.auth

import com.google.firebase.auth.FirebaseAuth
import com.jeevan.fall.data.auth.AuthRepository
import com.jeevan.fall.data.auth.models.CreateUser
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * If its a new account, it returns false,
 * else it return true.
 */
class CreateUserUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<CreateUser, Unit>(dispatcher) {
    override suspend fun execute(parameters: CreateUser) {
        auth.currentUser?.let {
            authRepository.createUser(it.uid, parameters)
        } ?: return
    }
}