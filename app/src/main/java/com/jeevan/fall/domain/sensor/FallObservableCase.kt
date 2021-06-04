package com.jeevan.fall.domain.sensor

import com.google.common.collect.EvictingQueue
import com.jeevan.fall.data.sensor.SensorRepository
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.FlowUseCase
import com.jeevan.fall.util.Result
import com.jeevan.fall.util.SENSOR_SAMPLE_RATE
import com.jeevan.fall.util.data
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * [FallObservableCase] is responsible for detecting the fall in the app.
 * It takes advantage of gyroscope, accelerometer and rotation angle to predict weather
 * if it was a fall or not. This algorithm is based on the research of Arkham Zahri
 *
 * @author jeevan s
 */
class FallObservableCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val repository: SensorRepository
) : FlowUseCase<Unit, Boolean>(dispatcher) {

    /**
     * Circular buffer to maintain last 3 second values of the gyro to predict max and min
     */
    private val acclLast: EvictingQueue<Float> =
        EvictingQueue.create((1000L / SENSOR_SAMPLE_RATE * 3).toInt())
    private val gyroLast: EvictingQueue<Float> =
        EvictingQueue.create((1000L / SENSOR_SAMPLE_RATE * 3).toInt())

    private val aMax get() = acclLast.maxOrNull() ?: 0F
    private val aMin get() = acclLast.minOrNull() ?: 0F
    private val gMax get() = gyroLast.maxOrNull() ?: 0F
    private val gMin get() = gyroLast.minOrNull() ?: 0F


    override fun execute(parameters: Unit): Flow<Result<Boolean>> {
        return try {
            val acclFlow = repository.getAccelerometerData()
                .sample(SENSOR_SAMPLE_RATE)
                .map {
                    it.data?.let {
                        val (x, y, z) = it
                        sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                    } ?: 0F
                }
            val gyroFlow = repository.getGyroscopeData()
                .sample(SENSOR_SAMPLE_RATE)
                .map {
                    it.data?.let {
                        val (x, y, z) = it
                        sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                    } ?: 0F
                }

            return acclFlow.combine(gyroFlow) { accl, gyro ->
                acclLast.add(accl)
                gyroLast.add(gyro)
                val (angX, angY, angZ) = repository.getRotationVectorData().first().data
                    ?: floatArrayOf(0F, 0F, 0F)
                if (accl < tM) {
                    return@combine false
                }
                return@combine if ((aMax - aMin) > tAt && (gMax - gMin) > tGt) {
                    max(angX, max(angY, angZ)) > tI
                } else false
            }.map { Result.Success(it) }
        } catch (e: Exception) {
            emptyFlow()
        }
    }

    companion object {
        const val tAt = 8.2F
        const val tGt = 6F
        const val tI = 0.6F
        const val tM = 9F
    }
}