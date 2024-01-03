package com.drconopoima.customsnowflakesequencekt;
import java.time.Instant;
import arrow.core.Either;

public class SequenceGenerator (
            var unused_bits: UByte, 
            var timestamp_bits: UByte,
            var node_id_bits: UByte,
            var sequence_bits: UByte,
            var custom_epoch: Instant,
            var micros_ten_power: UByte,
            var node_id: UShort
        ) {
    var current_window_initial_timestamp_nanos: Long = System.nanoTime();
    var now_timestamp_millis = System.currentTimeMillis();
    var sequence: UShort = (0).toUShort();
    var max_sequence: UShort = Math.pow((2).toDouble(),this.sequence_bits.toDouble()).toInt().toUShort();
    var timestamp_causality_incremental: UInt = (0).toUInt()
    var micros_power_adjustment_factor: Int = (Math.pow((10).toDouble(),this.micros_ten_power.toDouble()).toInt())*1_000
    var current_window_updated_timestamp_nanos: Long = System.nanoTime()
    var last_timestamp: UInt = ((this.current_window_updated_timestamp_nanos - this.current_window_initial_timestamp_nanos).toInt()/this.micros_power_adjustment_factor).toUInt()
    var current_timestamp: UInt = this.last_timestamp
    constructor( properties: SequenceProperties ) : this(
            unused_bits = properties.unused_bits,
            timestamp_bits = properties.timestamp_bits,
            node_id_bits = properties.node_id_bits,
            sequence_bits = properties.sequence_bits,
            custom_epoch = properties.custom_epoch,
            micros_ten_power = properties.micros_ten_power,
            node_id = properties.node_id
        ) {
    }
    fun generate_id() : Either<Exception,UInt> {
        // Guard clauses
        if (this.unused_bits >= (8).toUByte()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits for property 'unused_bits' should be smaller than 8."))
        }
        if (this.node_id_bits == (0).toUByte()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits for property 'node_id_bits' should be greater or equal than 1."))
        }
        if (this.node_id_bits > (16).toUByte()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits for property 'node_id_bits' should be smaller or equal than 16."))
        }
        if (this.sequence_bits == (0).toUByte()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits for property 'sequence_bits' should be greater or equal than 1."))
        }
        if (this.sequence_bits > (16).toUByte()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits for property 'sequence_bits' should be smaller or equal than 16."))
        }
        if ( (this.unused_bits+this.sequence_bits+this.node_id_bits+this.timestamp_bits) != (64).toUInt()) {
            return Either.Left(IllegalArgumentException("SequenceGeneratorGenerateIDError: Number of bits per generated ID needs to be exactly 64. Improperly specified bit components."))
        }
        if (this.sequence == this.max_sequence) {
            // TODO: when possible change for a version to wait next causality window (the provided micros-ten-power).
            // for now, it needs to be wasted a full millisecond because the Thread.sleep() accepts Int millis
            this.wait_next_millis_window()
        }


        return Either.Right(this.current_timestamp)
    }
    fun expired_millis_window() : Boolean {
        if (System.currentTimeMillis() != this.now_timestamp_millis) {
            return true
        }
        return false
    }
    fun wait_next_millis_window() {
        var current_timestamp_millis: Long = this.now_timestamp_millis;
        var current_timestamp: Long = System.currentTimeMillis();
        var sleep_for: Long = 1
        while ( current_timestamp <= current_timestamp_millis ) {
            Thread.sleep(sleep_for);
            current_timestamp = System.currentTimeMillis();
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleep_for*=2;
        }
        return
    }
    fun wait_current_millis_window() {
        var current_timestamp_millis: Long = this.now_timestamp_millis;
        var current_timestamp: Long = System.currentTimeMillis();
        var sleep_for: Long = 1
        while (current_timestamp < current_timestamp_millis) {
            Thread.sleep(sleep_for);
            current_timestamp = System.currentTimeMillis();
            // Double the cooldown wait period (exponential backoff). Useful if there was large clock backwards movement
            sleep_for*=2;
        }
        return
    }
}
