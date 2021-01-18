package ru.catcab.taximaster.paymentgateway

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
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASH
import ru.catcab.taximaster.paymentgateway.logic.*
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import java.time.LocalDateTime.now
import kotlin.time.ExperimentalTime
import kotlin.time.minutes


@ExperimentalTime
@KoinApiExtension
class Application : KoinComponent {
    private val flywayMigrationOperation by inject<FlywayMigrationOperation>()
    private val carDriverSynchronizationOperation by inject<CarDriverSynchronizationOperation>()
    private val driverSynchronizationOperation by inject<DriverSynchronizationOperation>()
    private val processPaymentsOperation by inject<PaymentsPollingOperation>()
    private val paymentInOperation by inject<PaymentInOperation>()
    private val logIdGenerator by inject<LogIdGenerator>()

    fun start(args: Array<String>) {
        LOG.info("args: ${args.toList()}")

        flywayMigrationOperation.activate()

        runBlocking {
            paymentInOperation.activate(SBERBANK_CASH, "1", "10.00".toBigDecimal(), "pay_id_${logIdGenerator.generate()}", now(), logIdGenerator.generate())
        }

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