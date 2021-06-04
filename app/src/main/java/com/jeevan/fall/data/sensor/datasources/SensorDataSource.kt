package com.jeevan.fall.data.sensor.datasources

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.jeevan.fall.di.AccelerometerSensor
import com.jeevan.fall.di.GyroscopeSensor
import com.jeevan.fall.di.RotationVector
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject

class SensorDataSource @Inject constructor(
    private val sensorManager: SensorManager,
    @AccelerometerSensor private val accelerometer: Sensor?,
    @GyroscopeSensor private val gyroscope: Sensor?,
    @RotationVector private val rotation: Sensor?
) {

    fun getAccelerometerData(): Flow<FloatArray> {
        val gravity = floatArrayOf(0F, 0F, 0F)
        val linearAcceleration = floatArrayOf(0F, 0F, 0F)
        if (accelerometer == null) {
            return emptyFlow()
        }
        return channelFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event !== null) {
                        /*
                         Creating a low pass filter to deal with acceleration caused due to gravity
                         */
                        val alpha = 0.8f
                        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                        linearAcceleration[0] = event.values[0] - gravity[0]
                        linearAcceleration[1] = event.values[1] - gravity[1]
                        linearAcceleration[2] = event.values[2] - gravity[2]

                        if(isActive) {
                            channel.offer(linearAcceleration)
                        }

                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    //
                }
            }
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

            awaitClose {
                sensorManager.unregisterListener(listener, accelerometer)
            }
        }
    }

    fun getGyroscopeData(): Flow<FloatArray> {
        if (gyroscope == null) {
            return emptyFlow()
        }
        return channelFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event !== null && isActive) {
                        channel.offer(event.values)
                    }

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    //
                }
            }
            sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
            awaitClose {
                sensorManager.unregisterListener(listener, gyroscope)
            }
        }
    }

    fun getRotation(): Flow<FloatArray> {
        if(rotation == null) {
            return emptyFlow()
        }
        return channelFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event !== null && isActive) {
                        channel.offer(event.values)
                    }

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    //
                }
            }
            sensorManager.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_GAME)
            awaitClose {
                sensorManager.unregisterListener(listener, rotation)
            }
        }

    }
}

