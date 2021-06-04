package com.jeevan.fall.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.sensor.FallObservableCase
import com.jeevan.fall.domain.sensor.RawAccelerometerObservableCase
import com.jeevan.fall.domain.sensor.RawGyroscopeObservableCase
import com.jeevan.fall.domain.sensor.RawRotationObservableCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject
constructor(
    val accelerometerObservable: RawAccelerometerObservableCase,
    val gyroscopeObservable: RawGyroscopeObservableCase,
    val rotationVectorObservable: RawRotationObservableCase,
    val fallObservableCase: FallObservableCase,
    @IoDispatcher val dispatcher: CoroutineDispatcher
) : ViewModel() {

    val accelerometerData by lazy {
        accelerometerObservable(Unit).asLiveData()
    }

    val gyroscopeData by lazy { gyroscopeObservable(Unit).asLiveData() }
    val rotationVector by lazy { rotationVectorObservable(Unit).asLiveData() }
    val isAFall by lazy { fallObservableCase(Unit).asLiveData() }

}