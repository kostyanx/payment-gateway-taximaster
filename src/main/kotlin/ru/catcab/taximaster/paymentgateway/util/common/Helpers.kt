package ru.catcab.taximaster.paymentgateway.util.common

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import java.math.BigInteger
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDateTime

object Helpers {
    val LEADING_ZEROES_REGEXP = "^0+(?!\$)".toRegex()

    val nowExpression = object : Expression<LocalDateTime>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { +"NOW()" }
    }

    fun String.removeLeadingZeros(): String {
        return replaceFirst(LEADING_ZEROES_REGEXP, "")
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun String.containsAddress(remoteHost: String): Boolean {
        val remoteAddress = Inet4Address.getByName(remoteHost)
        val (subnetAddress, subnetMaskCidr) = if (contains('/')) {
            val arr = split('/')
            Inet4Address.getByName(arr[0]) to arr[1].toInt()
        } else {
            Inet4Address.getByName(this) to 32
        }
        val subnetMaskInt = (0xFFFFFFFF ushr subnetMaskCidr).inv().toInt()
        val subnetAddressInt = ByteBuffer.wrap(subnetAddress.address).int
        val remoteAddressInt = ByteBuffer.wrap(remoteAddress.address).int
        return subnetAddressInt and subnetMaskInt == remoteAddressInt and subnetMaskInt
    }
}