package com.jeevan.fall.di

import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AccelerometerSensor

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class GyroscopeSensor

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class RotationVector
