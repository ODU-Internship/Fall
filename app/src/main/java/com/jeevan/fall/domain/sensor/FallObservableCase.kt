package com.jeevan.fall.domain.sensor

import com.google.common.collect.EvictingQueue
import com.jeevan.fall.data.sensor.SensorRepository
import com.jeevan.fall.data.sensor.models.FallLevel
import com.jeevan.fall.di.IoDispatcher
import com.jeevan.fall.domain.FlowUseCase
import com.jeevan.fall.util.Result
import com.jeevan.fall.util.SENSOR_SAMPLE_RATE
import com.jeevan.fall.util.data
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.abs
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
) : FlowUseCase<Unit, FallLevel>(dispatcher) {

    /**
     * Circular buffer to maintain last 3 second values of the gyro to predict max and min
     */
    private val acclLast: EvictingQueue<Float> =
        EvictingQueue.create((1000L / SENSOR_SAMPLE_RATE * 3).toInt())
    private val gyroLast: EvictingQueue<Float> =
        EvictingQueue.create((1000L / SENSOR_SAMPLE_RATE * 3).toInt())
    private val rotLast: EvictingQueue<Float> =
        EvictingQueue.create((1000L / SENSOR_SAMPLE_RATE * 3).toInt())

    private val aMax get() = acclLast.maxOrNull() ?: 0F
    private val aMin get() = acclLast.minOrNull() ?: 0F
    private val gMax get() = gyroLast.maxOrNull() ?: 0F
    private val gMin get() = gyroLast.minOrNull() ?: 0F
    private val rMax get() = rotLast.maxOrNull() ?: 0F
    private val rMin get() = rotLast.minOrNull() ?: 0F


    @FlowPreview
    override fun execute(parameters: Unit): Flow<Result<FallLevel>> {
        return try {
            val acclFlow = repository.getAccelerometerData()
                .sample(SENSOR_SAMPLE_RATE)
                .map {
                    it.data?.let {
                        val (x, y, z) = it
                        sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                    } ?: 0F
                }.cancellable()
            val gyroFlow = repository.getGyroscopeData()
                .sample(SENSOR_SAMPLE_RATE)
                .map {
                    it.data?.let {
                        val (x, y, z) = it
                        sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                    } ?: 0F
                }.cancellable()
            val rotFlow = repository.getRotationVectorData()
                .sample(SENSOR_SAMPLE_RATE)
                .map {
                    it.data?.let {
                        val (x, y, z) = it
                        sqrt(x.pow(2) + y.pow(2) + z.pow(2))
                    } ?: 0F
                }
                .cancellable()

            return combine(acclFlow, gyroFlow, rotFlow) { accl, gyro, rot ->
                acclLast.add(accl)
                gyroLast.add(gyro)
                rotLast.add(rot)

                if (accl < tM) {
                    return@combine FallLevel.None
                }
                return@combine if ((aMax - aMin) > tAt && (gMax - gMin) > tGt) {
                    if (abs(rMax - rMin) < 0.12) FallLevel.Change else FallLevel.Fall
                } else FallLevel.None
            }.map { Result.Success(it) }
        } catch (e: Exception) {
            throw e
        }
    }

    companion object {
        const val tAt = 7.2F
        const val tGt = 4F
        const val tM = 4F
    }
}