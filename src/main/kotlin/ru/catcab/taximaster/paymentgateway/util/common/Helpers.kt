package ru.catcab.taximaster.paymentgateway.util.common

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDateTime

object Helpers {
    val LEADING_ZEROES_REGEXP = "^0+(?!\$)".toRegex()

    fun String.removeLeadingZeros(): String {
        return replaceFirst(LEADING_ZEROES_REGEXP, "")
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    val nowExpression = object : Expression<LocalDateTime>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { +"NOW()" }
    }
}