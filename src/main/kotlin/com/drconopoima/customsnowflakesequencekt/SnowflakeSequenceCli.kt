package com.drconopoima.customsnowflakesequencekt;
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
    val custom_epoch: UInt by option("-e", "--custom-epoch", help="Custom epoch. Default: 1672531200000 or Jan 01 2023 00:00:00.").uint().default((1672531200000).toUInt())
    val node_id: UInt by option("-i", "--node-id", help="Numerical identifier for worker and datacenter information. Default: 0").uint().default((0).toUInt())
    val unused_bits: UInt by option("-u", "--unused-bits", help="Unused (sign) bits at the left-most of the sequence ID. Default: 0").uint().default((0).toUInt())
    val micros_ten_power: UInt by option("-m", "--micros-ten-power", help="Exponent multiplier base 10 in microseconds for timestamp. Default: 3 (operate in milliseconds)").uint().default((3).toUInt())
    val cooldown_ns: UInt by option("-c","--cooldown", help="Initial time in nanoseconds for exponential backoff wait after sequence is exhausted. Default: 1500 ns.").uint().default((1500).toUInt()) 
    override fun run() {
        System.out.println("Got quantity: '$quantity'")
        System.out.println("Got node-id-bits: '$node_id_bits'")
        System.out.println("Got sequence-bits: '$sequence_bits'")
        System.out.println("Got custom-epoch: '$custom_epoch'")
        System.out.println("Got node-id: '$node_id'")
        System.out.println("Got unused-bits: '$unused_bits'")
        System.out.println("Got micros-ten-power: '$micros_ten_power'")
        System.out.println("Got cooldown: '$cooldown_ns'")
    }
}

fun main(args: Array<String>) = SnowflakeSequenceCli().main(args)