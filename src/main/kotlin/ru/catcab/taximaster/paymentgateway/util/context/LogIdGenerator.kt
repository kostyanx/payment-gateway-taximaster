package ru.catcab.taximaster.paymentgateway.util.context

import java.net.InetAddress
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

class LogIdGenerator {
    private val hostByte = MessageDigest.getInstance("MD5").digest(InetAddress.getLocalHost().hostName.encodeToByteArray())[15]

    fun generate(): String {
        return generate(Random.nextLong())
    }

    fun generate(extId: Long): String {
        val randomPart = Random.nextBytes(4)
        randomPart[0] = hostByte
        val result = ByteBuffer.allocate(12).putLong(extId).put(randomPart)
        return Base64.getEncoder().encodeToString(result.array()).replace('+', '-').replace('/', '_')

    }
}

