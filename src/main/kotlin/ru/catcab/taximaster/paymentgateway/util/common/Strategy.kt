package ru.catcab.taximaster.paymentgateway.util.common

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
class Strategy(private val intervals: List<Interval>) {
    @Serializable
    data class Interval(val interval: Int, val quantity: Int)

    fun resolve(countOfErrors: Int): Interval? {
        var avail = countOfErrors
        for (interval in intervals) {
            if (avail <= interval.quantity) return interval
            avail -= interval.quantity
        }
        return null
    }

    fun retryRequired(countOfErrors: Int, errorTimestamp: LocalDateTime): Boolean {
        val interval = resolve(countOfErrors) ?: return false
        return errorTimestamp.plusSeconds(interval.interval.toLong()) <= LocalDateTime.now()
    }
}