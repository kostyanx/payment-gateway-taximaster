package ru.catcab.taximaster.paymentgateway

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.configuration.ApplicationConfiguration
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClient
import ru.catcab.taximaster.paymentgateway.service.flyway.FlywayMigrationService
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID
import ru.catcab.taximaster.paymentgateway.util.context.RequestIdGenerator


@KoinApiExtension
class Application : KoinComponent {
    private val taxiMasterApiClient by inject<TaxiMasterApiClient>()
    private val flywayMigrationService by inject<FlywayMigrationService>()

    fun start(args: Array<String>) {
        flywayMigrationService.applyMigrations()


        val requestIdGenerator = RequestIdGenerator()


        MDCKey.with(REQUEST_ID to requestIdGenerator.generate()) {
            val result = runBlocking(MDCContext()) {
//                taxiMasterApiClient.getDriverInfo(33)
                taxiMasterApiClient.getDriversInfo()
            }

            LOG.info("$result")

            if (result.code == 0) {
                println("OK")
            }
        }

        LOG.info("Hello ${args.toList()}")
    }

    companion object {
        private var LOG = LoggerFactory.getLogger(Application::class.java)!!

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