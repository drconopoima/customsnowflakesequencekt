package com.drconopoima.customsnowflakesequencekt
import java.time.Instant

data class SequenceProperties(
    var unusedBits: UByte,
    var nodeIdBits: UByte,
    var sequenceBits: UByte,
    var timestampBits: UByte,
    var customEpoch: Instant,
    var microsTenPower: UByte,
    val nodeId: UShort,
)
