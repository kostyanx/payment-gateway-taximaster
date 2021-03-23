package ru.catcab.taximaster.paymentgateway.configuration.values

import ru.catcab.taximaster.paymentgateway.database.enum.SourceType

data class Sberbank(
    val allowedSubnets: List<String>,
    val allowedHosts: List<String>,
    val allowedServValues: List<String>,
    val payChCash: List<String>,
    val payChCashless: List<String>,
    val payChDefault: SourceType
)