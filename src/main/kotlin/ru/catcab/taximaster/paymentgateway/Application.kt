package ru.catcab.taximaster.paymentgateway

import io.ktor.server.cio.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.configuration.ApplicationConfiguration
import ru.catcab.taximaster.paymentgateway.logic.CarDriverSynchronizationOperation
import ru.catcab.taximaster.paymentgateway.logic.DriverSynchronizationOperation
import ru.catcab.taximaster.paymentgateway.logic.FlywayMigrationOperation
import ru.catcab.taximaster.paymentgateway.logic.PaymentsPollingOperation
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime
import kotlin.time.minutes


@ExperimentalTime
@KoinApiExtension
class Application : KoinComponent {
    private val flywayMigrationOperation by inject<FlywayMigrationOperation>()
    private val carDriverSynchronizationOperation by inject<CarDriverSynchronizationOperation>()
    private val driverSynchronizationOperation by inject<DriverSynchronizationOperation>()
    private val processPaymentsOperation by inject<PaymentsPollingOperation>()

    fun start(args: Array<String>) {
        LOG.info("args: ${args.toList()}")

        flywayMigrationOperation.activate()

        thread { EngineMain.main(args) }

        runBlocking {
            while (true) {
                launch {
                    carDriverSynchronizationOperation.activate()
                    driverSynchronizationOperation.activate()
                    processPaymentsOperation.activate()
                }
                delay(1.minutes)
            }
        }

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