package ru.catcab.taximaster.paymentgateway.configuration.values

data class ApplicationConfig (
    val datasource: DataSourceGroup,
    val taximaster: TaxiMasterGroup,
    val retry: RetrySpec,
    val sberbank: Sberbank,
    val synchronization: Synchronization
)