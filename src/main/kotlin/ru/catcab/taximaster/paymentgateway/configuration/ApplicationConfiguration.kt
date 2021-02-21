package ru.catcab.taximaster.paymentgateway.configuration

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
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
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import javax.sql.DataSource

class ApplicationConfiguration {
    companion object {
        val module = module {
            single {
                ConfigLoader.Builder()
                    .addSource(PropertySource.file(File("application.yaml"), true))
                    .addSource(PropertySource.resource("/application.yaml"))
                    .build()
                    .loadConfigOrThrow<ApplicationConfig>()
            }

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

            single<ScheduledExecutorService> {
                ScheduledThreadPoolExecutor(1).apply {
                    removeOnCancelPolicy = true
                    Runtime.getRuntime().addShutdownHook(Thread(this::shutdown))
                }
            }

            single { Json.decodeFromString<Strategy>(get<ApplicationConfig>().retry.strategy) }

            single { FlywayMigrationService(get()) }

            single { FlywayMigrationOperation(get(), get()) }

            single { LogIdGenerator() }

            single { CarDriverSynchronizationOperation(get(), get(), get()) }

            single { DriverSynchronizationOperation(get(), get(), get()) }

            single { PaymentsPollingOperation(get(), get(), get(), get()) }

            single { PaymentInOperation(get(), get(), get()) }

            single { PaymentOutOperation(get(), get(), get(), get()) }

            single { SberbankCheckOperation(get(), get()) }

            single { SberbankPaymentOperation(get(), get(), get(), get()) }

            single { RequestLogOperation(get(), get()) }

            single { RemoveOldDataOperation(get(), get(), get(), get()) }
        }
    }
}