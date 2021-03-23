package ru.catcab.taximaster.paymentgateway.configuration.values

data class Ccb(
    val allowedSubnets: List<String>,
    val allowedHosts: List<String>,
    val allowedServValues: List<String>
)
