package ru.catcab.taximaster.paymentgateway.service.flyway

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class FlywayMigrationService(
    private val dataSource: DataSource
) {
    companion object {
        val LOG = LoggerFactory.getLogger(FlywayMigrationService::class.java)!!
    }

    fun applyMigrations() {
        try {
            Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate()
        } catch (e: Exception) {
            LOG.error("error on migration:", e)
        }
    }
}