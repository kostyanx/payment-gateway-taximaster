package ru.catcab.taximaster.paymentgateway

import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.h2.tools.Server
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.configuration.ApplicationConfiguration
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.logic.CarDriverSynchronizationOperation
import ru.catcab.taximaster.paymentgateway.logic.DriverSynchronizationOperation
import ru.catcab.taximaster.paymentgateway.logic.FlywayMigrationOperation
import ru.catcab.taximaster.paymentgateway.logic.PaymentsPollingOperation
import ru.catcab.taximaster.paymentgateway.logic.RemoveOldDataOperation
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


@KoinApiExtension
class Application : KoinComponent {
    private val config by inject<ApplicationConfig>()
    private val flywayMigrationOperation by inject<FlywayMigrationOperation>()
    private val carDriverSynchronizationOperation by inject<CarDriverSynchronizationOperation>()
    private val driverSynchronizationOperation by inject<DriverSynchronizationOperation>()
    private val processPaymentsOperation by inject<PaymentsPollingOperation>()
    private val removeOldDataOperation by inject<RemoveOldDataOperation>()

    @ExperimentalTime
    fun start(args: Array<String>) {
        LOG.info("args: ${args.toList()}")

        flywayMigrationOperation.activate()

        removeOldDataOperation.scheduleRemoveOldData()

        if (config.datasource.internal.startTcpServer) {
            val server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start()
            Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
        }

        thread { EngineMain.main(args) }

        val sync = config.synchronization

        val exceptionHandler = CoroutineExceptionHandler { ct, ex ->
            LOG.error("uncaught exception on coroutine $ct:", ex)
        }

        runBlocking {
            while (true) {
                launch(exceptionHandler) {
                    if (sync.crews) kotlin.runCatching { carDriverSynchronizationOperation.activate() }
                    if (sync.drivers) kotlin.runCatching { driverSynchronizationOperation.activate() }
                    kotlin.runCatching { processPaymentsOperation.activate() }
                }
                delay(sync.intervalSec.seconds)
            }
        }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Application::class.java)!!

        @ExperimentalTime
        @JvmStatic
        fun main(args: Array<String>) {
            val currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() ?: null
            Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
                LOG.error("uncaught exception on thread $thread:", ex)
                currentUncaughtExceptionHandler?.uncaughtException(thread, ex)
            }

            startKoin {
                slf4jLogger()
                modules(ApplicationConfiguration.module)
            }

            Application().start(args)

        }
    }
}