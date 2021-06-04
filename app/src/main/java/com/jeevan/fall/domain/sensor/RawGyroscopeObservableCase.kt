package com.jeevan.fall.domain.sensor

import com.jeevan.fall.data.sensor.SensorRepository
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.FlowUseCase
import com.jeevan.fall.util.Result
import com.jeevan.fall.util.SENSOR_SAMPLE_RATE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.sample
import javax.inject.Inject

class RawGyroscopeObservableCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val repository: SensorRepository
) : FlowUseCase<Unit, FloatArray>(dispatcher) {

    override fun execute(parameters: Unit): Flow<Result<FloatArray>> {
        return repository.getGyroscopeData().sample(SENSOR_SAMPLE_RATE)
    }
}