package ru.catcab.taximaster.paymentgateway.configuration.values

import ru.catcab.taximaster.paymentgateway.database.enum.SourceType

data class ApplicationConfig (
    val datasource: DataSourceGroup,
    val taximaster: TaxiMasterGroup,
    val retry: RetrySpec,
    val sberbank: Sberbank,
    val synchronization: Synchronization,
    val sourceTypeMap: Map<SourceType, String>,
    val logging: Logging
)