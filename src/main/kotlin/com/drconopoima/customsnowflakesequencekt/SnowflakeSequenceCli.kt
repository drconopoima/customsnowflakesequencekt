package com.drconopoima.customsnowflakesequencekt
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.*
import java.time.Instant

typealias ErrorResponse = String

class SnowflakeSequenceCli : CliktCommand() {
    val quantity: UInt by option("-q", "--quantity", help = "Amount/Quantity of sequence values requested.").uint().default((1).toUInt())
    val nodeIdBits: UInt by option(
        "-n",
        "--node-id-bits",
        help = "Bits used for storing worker and datacenter information. Default: 0-1023",
    ).uint().default((10).toUInt())
    val sequenceBits: UInt by option(
        "-s",
        "--sequence-bits",
        help = "Bits used for contiguous sequence values. Default: 0-4095",
    ).uint().default((12).toUInt())
    val customEpoch: String by option(
        "-e",
        "--custom-epoch",
        help = "Custom epoch. Default: '2023-01-01T00:00:00Z'",
    ).default("2023-01-01T00:00:00Z")
    val nodeId: UInt by option(
        "-i",
        "--node-id",
        help = "Numerical identifier for worker and datacenter information. Default: 0",
    ).uint().default((0).toUInt())
    val unusedBits: UInt by option(
        "-u",
        "--unused-bits",
        help = "Unused (sign) bits at the left-most of the sequence ID. Default: 0",
    ).uint().default((0).toUInt())
    val microsTenPower: UInt by option(
        "-m",
        "--micros-ten-power",
        help = "Exponent multiplier base 10 in microseconds for timestamp. Default: 3 (operate in milliseconds)",
    ).uint().default((3).toUInt())

    override fun run() {
        var unusedBits: UByte = unusedBits.toUByte()
        var nodeIdBits: UByte = nodeIdBits.toUByte()
        var sequenceBits: UByte = sequenceBits.toUByte()
        var timestampBits: UByte = ((64).toUByte() - sequenceBits - nodeIdBits - unusedBits).toUByte()
        var sequenceProperties =
            SequenceProperties(
                unusedBits,
                nodeIdBits,
                sequenceBits,
                timestampBits,
                Instant.parse(customEpoch),
                microsTenPower.toUByte(),
                nodeId.toUShort(),
            )
        var sequenceGenerator: SequenceGenerator = SequenceGenerator(sequenceProperties)
        System.out.println("Got quantity: '$quantity'")
        System.out.println("Got unused-bits: '${sequenceGenerator.unusedBits}'")
        System.out.println("Got node-id-bits: '${sequenceGenerator.nodeIdBits}'")
        System.out.println("Got sequence-bits: '${sequenceGenerator.sequenceBits}'")
        System.out.println("Got timestamp bits (derived): '${sequenceGenerator.timestampBits}'")
        System.out.println("Got custom-epoch: '${sequenceGenerator.customEpoch}'")
        System.out.println("Got node-id: '${sequenceGenerator.nodeId}'")
        System.out.println("Got micros-ten-power: '${sequenceGenerator.microsTenPower}'")
        System.out.println("Got max-sequence: '${sequenceGenerator.maxSequence}'")
        // System.out.println("Expired millis window: '${sequenceGenerator.isExpiredSystemMillis()}'")
        // sequenceGenerator.waitNextSystemMillis()
        // System.out.println("Expired millis window: '${sequenceGenerator.isExpiredSystemMillis()}'")
        // sequenceGenerator.unusedBits=(8).toUByte();
        // sequenceGenerator.sequenceBits=(0).toUByte();
        // sequenceGenerator.nodeIdBits=(0).toUByte();
        // sequenceGenerator.sequenceBits=(17).toUByte();
        // sequenceGenerator.nodeIdBits=(17).toUByte();
        // sequenceGenerator.timestampBits=(64).toUByte();
        System.out.println("Generated ID: ${sequenceGenerator.getId()}")
    }
}

fun main(args: Array<String>) = SnowflakeSequenceCli().main(args)
