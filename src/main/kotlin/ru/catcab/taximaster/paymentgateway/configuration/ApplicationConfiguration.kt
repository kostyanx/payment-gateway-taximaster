package ru.catcab.taximaster.paymentgateway.configuration

import com.sksamuel.hoplite.ConfigLoader
import org.koin.dsl.module
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClient

class ApplicationConfiguration {
    companion object {
        val module = module {
            single { ConfigLoader().loadConfigOrThrow<ApplicationConfig>("/application.yaml") }
            single {
                val config = get<ApplicationConfig>()
                TaxiMasterApiClient(config.taximaster.api.baseUrl, config.taximaster.api.secret)
            }
        }
    }
}