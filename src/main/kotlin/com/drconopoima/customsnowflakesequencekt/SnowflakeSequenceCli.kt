package com.drconopoima.customsnowflakesequencekt;
import java.time.Instant;
import com.github.ajalt.clikt.core.CliktCommand;
import com.github.ajalt.clikt.parameters.types.*;
import com.github.ajalt.clikt.parameters.options.default;
import com.github.ajalt.clikt.parameters.options.option;
import arrow.core.Either;

typealias ErrorResponse = String

class SnowflakeSequenceCli : CliktCommand() {
    val quantity: UInt by option("-q", "--quantity", help="Amount/Quantity of sequence values requested.").uint().default((1).toUInt())
    val node_id_bits: UInt by option("-n", "--node-id-bits", help="Bits used for storing worker and datacenter information. Default: 0-1023").uint().default((10).toUInt())
    val sequence_bits: UInt by option("-s", "--sequence-bits", help="Bits used for contiguous sequence values. Default: 0-4095").uint().default((12).toUInt())
    val custom_epoch: String by option("-e", "--custom-epoch", help="Custom epoch. Default: '2023-01-01T00:00:00Z'").default("2023-01-01T00:00:00Z")
    val node_id: UInt by option("-i", "--node-id", help="Numerical identifier for worker and datacenter information. Default: 0").uint().default((0).toUInt())
    val unused_bits: UInt by option("-u", "--unused-bits", help="Unused (sign) bits at the left-most of the sequence ID. Default: 0").uint().default((0).toUInt())
    val micros_ten_power: UInt by option("-m", "--micros-ten-power", help="Exponent multiplier base 10 in microseconds for timestamp. Default: 3 (operate in milliseconds)").uint().default((3).toUInt())
    val cooldown_ns: UInt by option("-c","--cooldown", help="Initial time in nanoseconds for exponential backoff wait after sequence is exhausted. Default: 500 ns.").uint().default((500).toUInt()) 
    override fun run() {
        var unused_bits: UByte = unused_bits.toUByte()
        var node_id_bits: UByte = node_id_bits.toUByte()
        var sequence_bits: UByte = sequence_bits.toUByte()
        var timestamp_bits: UInt = (64).toUByte()-sequence_bits-node_id_bits-unused_bits
        var sequence_properties = SequenceProperties(
            unused_bits,
            node_id_bits,
            sequence_bits,
            timestamp_bits.toUByte(),
            Instant.parse(custom_epoch),
            micros_ten_power.toUByte(),
            node_id.toUShort(),
            cooldown_ns.toUInt()
        )
        System.out.println("Got quantity: '$quantity'")
        System.out.println("Got unused-bits: '${sequence_properties.unused_bits}'")
        System.out.println("Got node-id-bits: '${sequence_properties.node_id_bits}'")
        System.out.println("Got sequence-bits: '${sequence_properties.sequence_bits}'")
        System.out.println("Got timestamp bits (derived): '${sequence_properties.timestamp_bits}'")
        System.out.println("Got custom-epoch: '${sequence_properties.custom_epoch}'")
        System.out.println("Got node-id: '${sequence_properties.node_id}'")
        System.out.println("Got micros-ten-power: '${sequence_properties.micros_ten_power}'")
        System.out.println("Got cooldown: '${sequence_properties.backoff_cooldown_start_ns}'")
    }
}

fun main(args: Array<String>) = SnowflakeSequenceCli().main(args)