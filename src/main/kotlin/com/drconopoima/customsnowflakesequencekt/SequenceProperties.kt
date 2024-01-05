package com.drconopoima.customsnowflakesequencekt
import java.time.Instant

data class SequenceProperties(
    var unused_bits: UByte,
    var node_id_bits: UByte,
    var sequence_bits: UByte,
    var timestamp_bits: UByte,
    var custom_epoch: Instant,
    var micros_ten_power: UByte,
    var node_id: UShort,
)
