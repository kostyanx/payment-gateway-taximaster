package ru.catcab.taximaster.paymentgateway.configuration.values

data class DatasourceSpec (
    val url: String,
    val username: String,
    val password: String,
    val startTcpServer: Boolean
)