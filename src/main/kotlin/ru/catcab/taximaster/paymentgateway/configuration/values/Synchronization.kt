package ru.catcab.taximaster.paymentgateway.configuration.values

data class Synchronization(
    val intervalSec: Int,
    val drivers: Boolean,
    val crews: Boolean
)
