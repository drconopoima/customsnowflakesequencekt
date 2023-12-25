data class SequenceProperties(
    var unused_bits: UByte, 
    var timestamp_bits: UByte,
    var node_id_bits: UByte,
    var sequence_bits: UByte,
    var custom_epoch: UInt,
    var current_timestamp: UInt,
    var last_timestamp: UInt,
    var micros_ten_power: UByte,
    var node_id: UShort,
    var sequence: UShort,
    var max_sequence: UShort,
    var backoff_cooldown_start_ns: UInt
);
