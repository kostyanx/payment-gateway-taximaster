package ru.catcab.taximaster.paymentgateway.configuration.values

data class ApplicationConfig (
    val datasource: DataSourceGroup,
    val taximaster: TaxiMasterGroup,
    val retry: RetrySpec,
    val allowedSubnets: List<String>,
    val allowedHosts: List<String>,
    val allowedServValues: List<String>
)