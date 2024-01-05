package com.drconopoima.customsnowflakesequencekt
import arrow.core.Either
import java.time.Instant

public class SequenceGenerator(
    var unused_bits: UByte,
    var timestamp_bits: UByte,
    var node_id_bits: UByte,
    var sequence_bits: UByte,
    var custom_epoch: Instant,
    var micros_ten_power: UByte,
    var node_id: UShort,
) {
    var now_systemtime_millis = System.currentTimeMillis()
    var current_window_initial_timestamp_nanos: Long = System.nanoTime()
    var micros_power_adjustment_factor: Int = (Math.pow((10).toDouble(), this.micros_ten_power.toDouble()).toInt()) * 1_000
    var sequence: UShort = (0).toUShort()
    var max_sequence: UShort = (Math.pow((2).toDouble(), this.sequence_bits.toDouble()).toLong() - 1).toUShort()
    var timestamp_causality_incremental: UShort = (0).toUShort()
    var now_systemtime_from_custom_epoch_millis = this.now_systemtime_millis - this.custom_epoch.toEpochMilli()
    var current_systemtime_nanos: Long = (this.now_systemtime_from_custom_epoch_millis.toDouble() * Math.pow((10).toDouble(), (6).toDouble())).toLong()
    var current_window_updated_timestamp_nanos: Long = System.nanoTime()
    var last_timestamp: ULong = (((this.current_systemtime_nanos + (this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos)).toDouble()) / this.micros_power_adjustment_factor).toULong()
    var current_timestamp: ULong = this.last_timestamp
    constructor(properties: SequenceProperties) : this(
        unused_bits = properties.unused_bits,
        timestamp_bits = properties.timestamp_bits,
        node_id_bits = properties.node_id_bits,
        sequence_bits = properties.sequence_bits,
        custom_epoch = properties.custom_epoch,
        micros_ten_power = properties.micros_ten_power,
        node_id = properties.node_id,
    ) {
    }

    fun generate_id(): Either<Exception, ULong> {
        // Guard clauses
        if (this.unused_bits >= (8).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'unused_bits' should be smaller than 8.",
                ),
            )
        }
        if (this.node_id_bits == (0).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'node_id_bits' should be greater or equal than 1.",
                ),
            )
        }
        if (this.node_id_bits > (16).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'node_id_bits' should be smaller or equal than 16.",
                ),
            )
        }
        if (this.sequence_bits == (0).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'sequence_bits' should be greater or equal than 1.",
                ),
            )
        }
        if (this.sequence_bits > (16).toUByte()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits for property 'sequence_bits' should be smaller or equal than 16.",
                ),
            )
        }
        if ((this.unused_bits + this.sequence_bits + this.node_id_bits + this.timestamp_bits) != (64).toUInt()) {
            return Either.Left(
                IllegalArgumentException(
                    "SequenceGeneratorGenerateIDError: Number of bits per generated ID needs to be exactly 64. Improperly specified bit components.",
                ),
            )
        }
        if (this.sequence == this.max_sequence) {
            // TODO: when possible change for a version to wait next causality window (the provided micros-ten-power).
            // for now, it needs to be wasted a full millisecond because the Thread.sleep() accepts Long millis
            this.wait_next_millis_window()
        }
        if (!this.expired_systemtime_millis_window()) {
            this.update_current_timestamp_unchecked()
        } else {
            this.new_systemtime_millis()
        }
        return Either.Right(this.current_timestamp)
    }

    fun update_current_timestamp_unchecked() {
        this.current_window_updated_timestamp_nanos = System.nanoTime()
        this.current_timestamp = (((this.current_systemtime_nanos + (this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos)).toDouble()) / this.micros_power_adjustment_factor).toULong()
        return
    }

    fun new_systemtime_millis() {
        var timestamp_millis_now = System.currentTimeMillis()
        if (this.now_systemtime_millis != timestamp_millis_now) {
            this.current_window_initial_timestamp_nanos = System.nanoTime()
            this.now_systemtime_millis = timestamp_millis_now
            this.now_systemtime_from_custom_epoch_millis = this.now_systemtime_millis - this.custom_epoch.toEpochMilli()
            this.current_systemtime_nanos = (this.now_systemtime_from_custom_epoch_millis.toDouble() * Math.pow((10).toDouble(), (6).toDouble())).toLong()
            this.current_window_updated_timestamp_nanos = System.nanoTime()
            var nanos_difference = this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos
            var systemtime_plus_nanos_difference = (this.current_systemtime_nanos + (this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos)).toDouble()
            this.last_timestamp = (((this.current_systemtime_nanos + (this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos)).toDouble()) / this.micros_power_adjustment_factor).toULong()
            this.current_timestamp = this.last_timestamp
            this.sequence = (0).toUShort()
            this.timestamp_causality_incremental = (0).toUShort()
        }
        return
    }

    fun expired_systemtime_millis_window(): Boolean {
        if (System.currentTimeMillis() != this.now_systemtime_millis) {
            return true
        }
        return false
    }

    fun wait_next_millis_window() {
        var current_timestamp: Long = System.currentTimeMillis()
        var sleep_for: Long = 1
        while (current_timestamp <= this.now_systemtime_millis) {
            Thread.sleep(sleep_for)
            current_timestamp = System.currentTimeMillis()
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleep_for *= 2
        }
        this.new_systemtime_millis()
        return
    }

    fun wait_current_millis_window() {
        var current_timestamp: Long = System.currentTimeMillis()
        var sleep_for: Long = 1
        while (current_timestamp < this.now_systemtime_millis) {
            Thread.sleep(sleep_for)
            current_timestamp = System.currentTimeMillis()
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleep_for *= 2
        }
        return
    }
}
