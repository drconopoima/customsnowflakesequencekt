package com.drconopoima.customsnowflakesequencekt;
import java.time.Instant;

public class SequenceGenerator (
            var unused_bits: UByte, 
            var timestamp_bits: UByte,
            var node_id_bits: UByte,
            var sequence_bits: UByte,
            var custom_epoch: Instant,
            var micros_ten_power: UByte,
            var node_id: UShort,
            var backoff_cooldown_start_ns: UInt
        ) {
    var current_window_initial_timestamp_nanos: Long = System.nanoTime();
    var now_timestamp_millis = System.currentTimeMillis();
    var current_window_updated_timestamp_nanos: Long = this.current_window_initial_timestamp_nanos;
    constructor( properties: SequenceProperties ) : this(
            unused_bits = properties.unused_bits,
            timestamp_bits = properties.timestamp_bits,
            node_id_bits = properties.node_id_bits,
            sequence_bits = properties.sequence_bits,
            custom_epoch = properties.custom_epoch,
            micros_ten_power = properties.micros_ten_power,
            node_id = properties.node_id,
            backoff_cooldown_start_ns = properties.backoff_cooldown_start_ns
        ) {
    }
}