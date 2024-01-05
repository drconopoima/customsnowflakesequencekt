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
    var nowSystemMillis = System.currentTimeMillis()
    var currentWindowInitialTimestampNanos: Long = System.nanoTime()
    var microsPowerFactor: Long = (Math.pow((10).toDouble(), this.microsTenPower.toDouble()).toLong()) * 1_000
    var sequence: UInt = (0).toUInt()
    var maxSequence: UInt = (Math.pow((2).toDouble(), this.sequenceBits.toDouble()).toLong() - 1).toUInt()
    var nowSystemCustomEpochNanos = ((this.nowSystemMillis - this.customEpoch.toEpochMilli()) * 1_000_000).toLong()
    var currentWindowUpdatedTimestampNanos: Long = System.nanoTime()
    var lastTimestamp: ULong = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsPowerFactor).toULong()
    var currentTimestamp: ULong = this.lastTimestamp
    var idTimestampCache: ULong = ((this.currentTimestamp shl (this.sequenceBits + this.nodeIdBits).toInt()).toULong() or this.nodeId.toULong())
    constructor(properties: SequenceProperties) : this(
        unusedBits = properties.unusedBits,
        timestampBits = properties.timestampBits,
        nodeIdBits = properties.nodeIdBits,
        sequenceBits = properties.sequenceBits,
        customEpoch = properties.customEpoch,
        microsTenPower = properties.microsTenPower,
        nodeId = properties.nodeId,
    ) {
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
            this.waitNextSystemMillis()
        }
        if (!this.isExpiredSystemMillis()) {
            this.updateCurrentTimestampUnchecked()
        } else {
            this.initNewSystemMillis()
        }
        if (this.currentTimestamp != this.lastTimestamp) {
            this.cacheIdTimestamp()
        }

        var newId: ULong = (this.idTimestampCache or (this.sequence.toULong() shl this.nodeIdBits.toInt()).toULong())
        this.sequence += (1).toUInt()
        return Either.Right(newId)
    }

    fun updateCurrentTimestampUnchecked() {
        this.currentWindowUpdatedTimestampNanos = System.nanoTime()
        this.currentTimestamp = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsPowerFactor).toULong()
        return
    }

    fun initNewSystemMillis() {
        var currentTimeMillis = System.currentTimeMillis()
        if (this.nowSystemMillis != currentTimeMillis) {
            this.currentWindowInitialTimestampNanos = System.nanoTime()
            this.nowSystemMillis = currentTimeMillis
            this.nowSystemCustomEpochNanos = ((this.nowSystemMillis - this.customEpoch.toEpochMilli()) * 1_000_000).toLong()
            this.currentWindowUpdatedTimestampNanos = System.nanoTime()
            this.lastTimestamp = (((this.nowSystemCustomEpochNanos + (this.currentWindowUpdatedTimestampNanos - this.currentWindowInitialTimestampNanos)).toDouble()) / this.microsPowerFactor).toULong()
            this.currentTimestamp = this.lastTimestamp
        }
        return
    }

    fun isExpiredSystemMillis(): Boolean {
        if (System.currentTimeMillis() != this.nowSystemMillis) {
            return true
        }
        return false
    }

    fun waitNextSystemMillis() {
        var currentTimestamp: Long = System.currentTimeMillis()
        var sleepFor: Long = 1
        while (currentTimestamp <= this.nowSystemMillis) {
            Thread.sleep(sleepFor)
            currentTimestamp = System.currentTimeMillis()
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleepFor *= 2
        }
        this.initNewSystemMillis()
        return
    }

    fun cacheIdTimestamp() {
        this.idTimestampCache = (this.currentTimestamp shl (this.sequenceBits + this.nodeIdBits).toInt()).toULong()
        this.lastTimestamp = this.currentTimestamp
        this.idTimestampCache = this.idTimestampCache or this.nodeId.toULong()
        this.sequence = (0).toUInt()
    }
}
