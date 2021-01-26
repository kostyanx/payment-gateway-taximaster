package ru.catcab.taximaster.paymentgateway.configuration.values

data class Sberbank(
    val security: Security,
    val allowedServValues: List<String>,
    val payChCash: List<String>,
    val payChCashless: List<String>
) {
    data class Security(
        val allowedSubnets: List<String>,
        val allowedHosts: List<String>
    )
}