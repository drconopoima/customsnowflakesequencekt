package com.drconopoima.customsnowflakesequencekt;
import java.time.Instant;
import com.github.ajalt.clikt.core.CliktCommand;
import com.github.ajalt.clikt.parameters.types.*;
import com.github.ajalt.clikt.parameters.options.default;
import com.github.ajalt.clikt.parameters.options.option;

typealias ErrorResponse = String

class SnowflakeSequenceCli : CliktCommand() {
    val quantity: UInt by option("-q", "--quantity", help="Amount/Quantity of sequence values requested.").uint().default((1).toUInt())
    val node_id_bits: UInt by option("-n", "--node-id-bits", help="Bits used for storing worker and datacenter information. Default: 0-1023").uint().default((10).toUInt())
    val sequence_bits: UInt by option("-s", "--sequence-bits", help="Bits used for contiguous sequence values. Default: 0-4095").uint().default((12).toUInt())
    val custom_epoch: String by option("-e", "--custom-epoch", help="Custom epoch. Default: '2023-01-01T00:00:00Z'").default("2023-01-01T00:00:00Z")
    val node_id: UInt by option("-i", "--node-id", help="Numerical identifier for worker and datacenter information. Default: 0").uint().default((0).toUInt())
    val unused_bits: UInt by option("-u", "--unused-bits", help="Unused (sign) bits at the left-most of the sequence ID. Default: 0").uint().default((0).toUInt())
    val micros_ten_power: UInt by option("-m", "--micros-ten-power", help="Exponent multiplier base 10 in microseconds for timestamp. Default: 3 (operate in milliseconds)").uint().default((3).toUInt())
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
            node_id.toUShort()
        )
        var sequence_generator : SequenceGenerator = SequenceGenerator(sequence_properties);
        System.out.println("Got quantity: '$quantity'")
        System.out.println("Got unused-bits: '${sequence_generator.unused_bits}'")
        System.out.println("Got node-id-bits: '${sequence_generator.node_id_bits}'")
        System.out.println("Got sequence-bits: '${sequence_generator.sequence_bits}'")
        System.out.println("Got timestamp bits (derived): '${sequence_generator.timestamp_bits}'")
        System.out.println("Got custom-epoch: '${sequence_generator.custom_epoch}'")
        System.out.println("Got node-id: '${sequence_generator.node_id}'")
        System.out.println("Got micros-ten-power: '${sequence_generator.micros_ten_power}'")
        System.out.println("Got max-sequence: '${sequence_generator.max_sequence}'")
        // System.out.println("Expired millis window: '${sequence_generator.expired_millis_window()}'")
        // sequence_generator.wait_next_millis_window()
        // System.out.println("Expired millis window: '${sequence_generator.expired_millis_window()}'")
        // sequence_generator.unused_bits=(8).toUByte();
        // sequence_generator.sequence_bits=(0).toUByte();
        // sequence_generator.node_id_bits=(0).toUByte();
        // sequence_generator.sequence_bits=(17).toUByte();
        // sequence_generator.node_id_bits=(17).toUByte();
        // sequence_generator.timestamp_bits=(64).toUByte();
        System.out.println("Generated ID: ${sequence_generator.generate_id()}");
    }
}

fun main(args: Array<String>) = SnowflakeSequenceCli().main(args)