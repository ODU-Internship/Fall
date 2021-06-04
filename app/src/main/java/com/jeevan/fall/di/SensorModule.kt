package com.jeevan.fall.di

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @AccelerometerSensor
    fun provideAccelerometer(sensorManager: SensorManager): Sensor? {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    @Provides
    @GyroscopeSensor
    fun provideGyroscope(sensorManager: SensorManager): Sensor? {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    @Provides
    @RotationVector
    fun provideRotationVector(sensorManager: SensorManager): Sensor? {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }
}