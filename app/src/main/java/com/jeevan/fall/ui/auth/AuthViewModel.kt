package com.jeevan.fall.ui.auth

import android.app.Activity
import androidx.lifecycle.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.jeevan.fall.data.auth.models.CreateUser
import com.jeevan.fall.domain.auth.CreateUserUseCase
import com.jeevan.fall.domain.auth.SignInUseCase
import com.jeevan.fall.util.AUTH_OTP_TIMEOUT
import com.jeevan.fall.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val signIn: SignInUseCase,
    private val createUser: CreateUserUseCase
) : ViewModel() {
    /**
     * Entire Auth Verification flow
     */
    private val _otpState = MutableLiveData(OtpStates.READY)
    val otpState = _otpState as LiveData<OtpStates>

    fun setOtpState(state: OtpStates) {
        _otpState.value = state
    }

    enum class OtpStates {
        READY,
        RUNNING,
        CODE_SENT,
        REFRESH_ALLOWED,
        AUTO_VERIFICATION_COMPLETE,
        VERIFY_START,
        VERIFY_RUNNING,
        VERIFY_COMPLETE,
        VERIFY_FAILED_INVALID_CREDENTIALS,
        VERIFY_FAILED_TOO_MANY_REQUESTS,
        VERIFY_FAILED_INVALID_CODE,
        VERIFY_FAILED_UNKNOWN
    }

    /**
     * Mobile number section
     */
    var phone: String = ""

    /**
     * OTP section
     */
    var verificationId: String = ""
    var refreshToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var credential: PhoneAuthCredential
    val countDown: LiveData<String> = otpState.switchMap {
        return@switchMap when (it) {
            OtpStates.CODE_SENT ->
                (AUTH_OTP_TIMEOUT downTo 1)
                    .asSequence()
                    .asFlow()
                    .map { s -> delay(1000); s.toString() }
                    .asLiveData()
            else -> {
                object : LiveData<String>("") {}
            }
        }
    }
    private val verificationStateCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                this@AuthViewModel.credential = credential
                setOtpState(OtpStates.AUTO_VERIFICATION_COMPLETE)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        setOtpState(OtpStates.VERIFY_FAILED_INVALID_CREDENTIALS)
                    }
                    is FirebaseTooManyRequestsException -> {
                        setOtpState(OtpStates.VERIFY_FAILED_TOO_MANY_REQUESTS)
                    }
                    else -> {
                        setOtpState(OtpStates.VERIFY_FAILED_UNKNOWN)
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthViewModel.verificationId = verificationId
                this@AuthViewModel.refreshToken = forceResendingToken
                this@AuthViewModel.setOtpState(OtpStates.CODE_SENT)
            }
        }

    // its not so dangerous as you think it is
    fun createCredentials(phone: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth).apply {
            setPhoneNumber(phone)
            setTimeout(AUTH_OTP_TIMEOUT, TimeUnit.SECONDS)
            setActivity(activity)
            refreshToken?.let { setForceResendingToken(it) }
            setCallbacks(verificationStateCallbacks)
        }.build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        setOtpState(OtpStates.RUNNING)
    }

    fun setupToken(code: String) {
        credential = PhoneAuthProvider.getCredential(verificationId, code)
        setOtpState(OtpStates.VERIFY_START)
    }

    /**
     * Loading section
     */
    enum class SignInState {
        ATTEMPT,
        COMPLETE_NEW,
        COMPLETE_EXISTING,
        FAILED
    }

    private val _signInAttemptResult = MutableLiveData<Result<Boolean>>()
    val signInAttemptResult: LiveData<SignInState> = _signInAttemptResult.map {
        it?.let {
            when (it) {
                is Result.Loading -> {
                    SignInState.ATTEMPT
                }
                is Result.Success -> {
                    setOtpState(OtpStates.VERIFY_COMPLETE)
                    if (it.data) SignInState.COMPLETE_EXISTING
                    else SignInState.COMPLETE_NEW
                }
                is Result.Error -> {
                    if (it.exception is FirebaseAuthInvalidCredentialsException)
                        setOtpState(OtpStates.VERIFY_FAILED_INVALID_CODE)
                    else
                        setOtpState(OtpStates.VERIFY_FAILED_UNKNOWN)
                    SignInState.FAILED
                }

            }
        } ?: SignInState.ATTEMPT
    }

    fun signInUser() {
        setOtpState(OtpStates.VERIFY_RUNNING)
        viewModelScope.launch {
            val result = signIn(credential)
            _signInAttemptResult.value = result
        }
    }

    /**
     * Details section
     *
     */
    private val _updateDetailsResult = MutableLiveData<Result<Unit>>()
    val updateDetailsResult = _updateDetailsResult as LiveData<Result<Unit>>

    fun updateDetails(name: String, age: Int, address: String) {
        viewModelScope.launch {
            _updateDetailsResult.value = Result.Loading
            val result = createUser(CreateUser(name, age, address))
            _updateDetailsResult.value = result
        }
    }

}