package ru.catcab.taximaster.paymentgateway

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import ru.catcab.taximaster.paymentgateway.configuration.ApplicationConfiguration
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClient


@KoinApiExtension
class Application : KoinComponent {
    private val taxiMasterApiClient by inject<TaxiMasterApiClient>()

    fun start(args: Array<String>) {

        if (2 > 1) {

            MDC.put("app", "pg-tm")

            val result = runBlocking(MDCContext()) {
                taxiMasterApiClient.getCrewInfo(33)
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
                printLogger()
                modules(ApplicationConfiguration.module)
            }

            Application().start(args)

        }
    }
}