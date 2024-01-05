package com.drconopoima.customsnowflakesequencekt
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.*
import java.time.Instant

typealias ErrorResponse = String

class SnowflakeSequenceCli : CliktCommand() {
    val quantity: Int by option("-q", "--quantity", help = "Amount/Quantity of sequence values requested.").int().default((1).toInt())
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
    val debug by option(
        "-d",
        "--debug",
        help = "Show properties used from defailts or CLI arguments provided",
    ).flag(
        "--no-debug",
        default = false,
    )
    val time by option(
        "-t",
        "--time",
        help = "Show properties used from defailts or CLI arguments provided",
    ).flag(
        "--no-time",
        default = false,
    )

    override fun run() {
        var quantity: Int = quantity
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
        if (debug)
            {
                System.out.println("Got quantity: '$quantity'")
                System.out.println("Got unused-bits: '${sequenceGenerator.unusedBits}'")
                System.out.println("Got node-id-bits: '${sequenceGenerator.nodeIdBits}'")
                System.out.println("Got sequence-bits: '${sequenceGenerator.sequenceBits}'")
                System.out.println("Got timestamp bits (derived): '${sequenceGenerator.timestampBits}'")
                System.out.println("Got custom-epoch: '${sequenceGenerator.customEpoch}'")
                System.out.println("Got node-id: '${sequenceGenerator.nodeId}'")
                System.out.println("Got micros-ten-power: '${sequenceGenerator.microsTenPower}'")
                System.out.println("Got max-sequence: '${sequenceGenerator.maxSequence}'")
            }
        var idList = arrayOfNulls<ULong>(quantity);
        var initialSystemNanosTime: Long = 0
        var finalSystemNanosTime: Long = 0
        if (time) {
            initialSystemNanosTime = System.nanoTime()
        }
        for (i: Int in 0 until idList.size) {
            var newId = sequenceGenerator.getId()
            idList[i] = newId.fold({ (0).toULong() }, { it })
        }
        if (time) {
            finalSystemNanosTime = System.nanoTime()
        }
        for ( id in idList ) {
            System.out.println("$id")
        }
        if (time) {
            var elapsed: Long = (finalSystemNanosTime - initialSystemNanosTime)
            var elapsedPerId: Double = elapsed.toDouble() / quantity.toDouble()
            System.out.println("It took ${elapsed ?: 0} nanoseconds, time per id: ${elapsedPerId ?: 0} ns")
        }
    }
}

fun main(args: Array<String>) = SnowflakeSequenceCli().main(args)
