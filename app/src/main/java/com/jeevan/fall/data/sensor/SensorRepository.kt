package com.jeevan.fall.data.sensor

import com.jeevan.fall.data.sensor.datasources.SensorDataSource
import com.jeevan.fall.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SensorRepository @Inject constructor(private val sensorDataSource: SensorDataSource) {
    fun getAccelerometerData(): Flow<Result<FloatArray>> {
        return sensorDataSource.getAccelerometerData().map { Result.Success(it) }
    }

    fun getGyroscopeData(): Flow<Result<FloatArray>> {
        return sensorDataSource.getGyroscopeData().map { Result.Success(it) }
    }

    fun getRotationVectorData(): Flow<Result<FloatArray>> {
        return sensorDataSource.getRotation().map { Result.Success(it) }
    }
}