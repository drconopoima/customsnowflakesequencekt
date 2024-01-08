package com.drconopoima.customsnowflakesequencekt
import arrow.core.Either
import java.time.Instant

public class SequenceGenerator(
    var unusedBits: UByte,
    var timestampBits: UByte,
    var nodeIdBits: UByte,
    var sequenceBits: UByte,
    var customEpoch: Instant,
    var microsTenPower: UByte,
    val nodeId: UShort,
) {
    var microsToNanosFactor: Long = (Math.pow((10).toDouble(), this.microsTenPower.toDouble()).toLong()) * 1_000
    var millisToSystemTimeFrameFactor: Long = 1
    var nowSystemTimeFrame: Long = System.currentTimeMillis() / this.millisToSystemTimeFrameFactor
    var currentWindowInitialTimestampNanos: Long = System.nanoTime()
    var sequence: UInt = (0).toUInt()
    var maxSequence: UInt = (Math.pow((2).toDouble(), this.sequenceBits.toDouble()).toLong() - 1).toUInt()
    var nowSystemCustomEpochNanos = ((this.nowSystemTimeFrame - ( this.customEpoch.toEpochMilli() / this.millisToSystemTimeFrameFactor)) * 1_000_000 * this.millisToSystemTimeFrameFactor).toLong()
    var currentWindowUpdatedTimestampNanos: Long = System.nanoTime()
    var lastTimestamp: ULong = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsToNanosFactor).toULong()
    var currentTimestamp: ULong = this.lastTimestamp
    constructor(properties: SequenceProperties) : this(
        unusedBits = properties.unusedBits,
        timestampBits = properties.timestampBits,
        nodeIdBits = properties.nodeIdBits,
        sequenceBits = properties.sequenceBits,
        customEpoch = properties.customEpoch,
        microsTenPower = properties.microsTenPower,
        nodeId = properties.nodeId,
    ) {
        if ( this.microsTenPower <= (3).toUByte() ) {
            this.millisToSystemTimeFrameFactor = 1
        } else {
            this.millisToSystemTimeFrameFactor = ( this.microsToNanosFactor / 1_000_000)
        }
        this.nowSystemTimeFrame = System.currentTimeMillis() / this.millisToSystemTimeFrameFactor
        this.currentWindowInitialTimestampNanos = System.nanoTime()
        this.maxSequence = (Math.pow((2).toDouble(), this.sequenceBits.toDouble()).toLong() - 1).toUInt()
        this.nowSystemCustomEpochNanos = ((this.nowSystemTimeFrame - ( this.customEpoch.toEpochMilli() / this.millisToSystemTimeFrameFactor)) * 1_000_000 * this.millisToSystemTimeFrameFactor).toLong()
        this.currentWindowUpdatedTimestampNanos = System.nanoTime()
        this.lastTimestamp = (((this.nowSystemCustomEpochNanos).toDouble()) / this.microsToNanosFactor).toULong()
        this.currentTimestamp = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsToNanosFactor).toULong()
    }

    fun getId(): Either<Exception, ULong> {
        // Guard clauses
        if (this.unusedBits >= (8).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'unusedBits' should be smaller than 8.",
                ),
            )
        }
        if (this.nodeIdBits == (0).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'nodeIdBits' should be greater or equal than 1.",
                ),
            )
        }
        if (this.nodeIdBits > (16).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'nodeIdBits' should be smaller or equal than 16.",
                ),
            )
        }
        if (this.sequenceBits == (0).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'sequenceBits' should be greater or equal than 1.",
                ),
            )
        }
        if (this.sequenceBits > (16).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'sequenceBits' should be smaller or equal than 16.",
                ),
            )
        }
        if ((this.unusedBits + this.sequenceBits + this.nodeIdBits + this.timestampBits) != (64).toUInt()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits per generated ID needs to be exactly 64. Improperly specified bit components.",
                ),
            )
        }
        if (this.sequence == this.maxSequence) {
            // TODO: when possible change for a version to wait next causality window (the provided micros-ten-power).
            // for now, it needs to be wasted a full millisecond because the Thread.sleep() accepts Long millis
            this.waitNextSystemTimeFrame()
        }
        if (!this.isExpiredSystemTimeFrame()) {
            this.updateCurrentTimestampUnchecked()
        } else {
            this.initNewSystemTimeFrame()
            this.updateCurrentTimestampUnchecked()
        }
        var newId: ULong = (((this.currentTimestamp shl (this.sequenceBits + this.nodeIdBits).toInt()).toULong() or this.nodeId.toULong()) or (this.sequence.toULong() shl this.nodeIdBits.toInt()).toULong())
        this.sequence += (1).toUInt()
        return Either.Right(newId)
    }

    fun updateCurrentTimestampUnchecked() {
        this.currentWindowUpdatedTimestampNanos = System.nanoTime()
        this.currentTimestamp = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsToNanosFactor).toULong()
        if ( this.currentTimestamp != this.lastTimestamp ) {
            this.sequence = (0).toUInt()
        }
        return
    }

    fun initNewSystemTimeFrame() {
        this.nowSystemTimeFrame = System.currentTimeMillis() / this.millisToSystemTimeFrameFactor
        this.currentWindowInitialTimestampNanos = System.nanoTime()
        this.nowSystemCustomEpochNanos = ((this.nowSystemTimeFrame - ( this.customEpoch.toEpochMilli() / this.millisToSystemTimeFrameFactor)) * 1_000_000 * this.millisToSystemTimeFrameFactor ).toLong()
        this.lastTimestamp = ((this.nowSystemCustomEpochNanos).toDouble() / this.microsToNanosFactor).toULong()
        this.sequence = (0).toUInt()
        return
    }

    fun isExpiredSystemTimeFrame(): Boolean {
        if ( (System.currentTimeMillis() / this.millisToSystemTimeFrameFactor ) != this.nowSystemTimeFrame) {
            return true
        }
        return false
    }

    fun waitNextSystemTimeFrame() {
        var currentTimestamp: Long = System.currentTimeMillis() / this.millisToSystemTimeFrameFactor
        var sleepFor: Long = 1
        while (currentTimestamp <= this.nowSystemTimeFrame) {
            Thread.sleep(sleepFor)
            currentTimestamp = System.currentTimeMillis() / this.millisToSystemTimeFrameFactor
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleepFor *= 2
        }
        this.initNewSystemTimeFrame()
        return
    }
}
