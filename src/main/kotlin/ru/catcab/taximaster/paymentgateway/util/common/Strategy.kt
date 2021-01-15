package ru.catcab.taximaster.paymentgateway.util.common

import kotlinx.serialization.Serializable

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
}