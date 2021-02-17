package ru.catcab.taximaster.paymentgateway.configuration.values

import ru.catcab.taximaster.paymentgateway.database.enum.SourceType

data class Sberbank(
    val security: Security,
    val allowedServValues: List<String>,
    val payChCash: List<String>,
    val payChCashless: List<String>,
    val payChDefault: SourceType
) {
    data class Security(
        val allowedSubnets: List<String>,
        val allowedHosts: List<String>
    )
}