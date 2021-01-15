package ru.catcab.taximaster.paymentgateway.configuration

import com.sksamuel.hoplite.ConfigLoader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.h2.jdbcx.JdbcConnectionPool
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.logic.*
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClient
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClientAdapter
import ru.catcab.taximaster.paymentgateway.service.flyway.FlywayMigrationService
import ru.catcab.taximaster.paymentgateway.util.common.Strategy
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import javax.sql.DataSource

class ApplicationConfiguration {
    companion object {
        val module = module {
            single { ConfigLoader().loadConfigOrThrow<ApplicationConfig>("/application.yaml") }

            single {
                val config = get<ApplicationConfig>().taximaster.api
                TaxiMasterApiClient(config.baseUrl, config.secret)
            }

            single { TaxiMasterApiClientAdapter(get()) }

            single<DataSource> {
                val config = get<ApplicationConfig>().datasource.internal
                JdbcConnectionPool.create(config.url, config.username, config.password)!!
            }

            single {
                val dataSource = get<DataSource>()
                Database.connect(dataSource, { it.autoCommit = true })
            }

            single { Json.decodeFromString<Strategy>(get<ApplicationConfig>().retry.strategy) }

            single { FlywayMigrationService(get()) }

            single { LogIdGenerator() }

            single { CarDriverSynchronizationOperation(get(), get(), get()) }

            single { DriverSynchronizationOperation(get(), get(), get()) }

            single { PaymentsPollingOperation(get(), get(), get(), get()) }

            single { PaymentInOperation(get()) }

            single { PaymentOutOperation(get(), get(), get()) }
        }
    }
}