package com.lainovic.tomtom.straycat.domain.simulation

import android.os.Parcel
import android.os.Parcelable
import com.tomtom.quantity.Distance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class SimulationConfiguration(
    val useRealisticTiming: Boolean = false,
    val delayBetweenEmissions: Duration = 1000.milliseconds,
    val distanceBetweenEmissions: Distance = Distance.ZERO,
    val loopIndefinitely: Boolean = false,
    val speedMultiplier: Float = 1f,
    val noiseLevelInMeters: Float = 0f,
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        useRealisticTiming = parcel.readByte() != 0.toByte(),
        delayBetweenEmissions = parcel.readLong().milliseconds,
        distanceBetweenEmissions = Distance.meters(parcel.readDouble()),
        loopIndefinitely = parcel.readByte() != 0.toByte(),
        speedMultiplier = parcel.readFloat(),
        noiseLevelInMeters = parcel.readFloat()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (useRealisticTiming) 1 else 0)
        dest.writeLong(delayBetweenEmissions.inWholeMilliseconds)
        dest.writeDouble(distanceBetweenEmissions.inMeters())
        dest.writeByte(if (loopIndefinitely) 1 else 0)
        dest.writeFloat(speedMultiplier)
        dest.writeFloat(noiseLevelInMeters)
    }

    companion object CREATOR : Parcelable.Creator<SimulationConfiguration> {
        override fun createFromParcel(parcel: Parcel): SimulationConfiguration {
            return SimulationConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<SimulationConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}

data class MutableSimulationConfiguration(
    val useRealisticTiming: Boolean = false,
    var delayBetweenEmissions: Duration = 1000.milliseconds,
    var distanceBetweenEmissions: Distance = Distance.ZERO,
    var loopIndefinitely: Boolean = false,
    var speedMultiplier: Float = 1f,
    var noiseLevelInMeters: Float = 0f,
)