package ru.catcab.taximaster.paymentgateway

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.configuration.ApplicationConfiguration
import ru.catcab.taximaster.paymentgateway.logic.CarDriverSynchronizationOperation
import ru.catcab.taximaster.paymentgateway.service.flyway.FlywayMigrationService


@KoinApiExtension
class Application : KoinComponent {
    private val flywayMigrationService by inject<FlywayMigrationService>()
    private val carDriverSynchronizationOperation by inject<CarDriverSynchronizationOperation>()

    fun start(args: Array<String>) {
        LOG.info("args: ${args.toList()}")

        flywayMigrationService.applyMigrations()

        carDriverSynchronizationOperation.activate()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Application::class.java)!!

        @JvmStatic
        fun main(args: Array<String>) {
            startKoin {
                slf4jLogger()
                modules(ApplicationConfiguration.module)
            }

            Application().start(args)

        }
    }
}