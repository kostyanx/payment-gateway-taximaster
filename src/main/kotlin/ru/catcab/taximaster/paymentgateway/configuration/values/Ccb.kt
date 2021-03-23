package ru.catcab.taximaster.paymentgateway.configuration.values

data class Ccb(
    val checkSign: Boolean,
    val secret: String,
    val allowedSubnets: List<String>,
    val allowedHosts: List<String>,
    val allowedServValues: List<String>
)
