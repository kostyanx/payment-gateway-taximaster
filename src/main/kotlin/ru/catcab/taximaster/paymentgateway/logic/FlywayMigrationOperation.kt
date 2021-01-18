package ru.catcab.taximaster.paymentgateway.logic

import ru.catcab.taximaster.paymentgateway.service.flyway.FlywayMigrationService
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.FLYWAY_MIGRATION
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger

class FlywayMigrationOperation(
    private val flywayMigrationService: FlywayMigrationService,
    private val logIdGenerator: LogIdGenerator
) {
    private val methodLogger = MethodLogger()

    fun activate() {
        methodLogger.logMethod(this::activate) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to FLYWAY_MIGRATION.value)
        }?.let { return it() }

        flywayMigrationService.applyMigrations()
    }
}