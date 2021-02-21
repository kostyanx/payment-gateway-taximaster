package ru.catcab.taximaster.paymentgateway.logic

import com.github.shyiko.skedule.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.database.entity.RequestLog
import ru.catcab.taximaster.paymentgateway.database.entity.RequestLog.inserted
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.REMOVE_OLD_DATA
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RemoveOldDataOperation(
    private val config: ApplicationConfig,
    private val executor: ScheduledExecutorService,
    private val database: Database,
    private val logIdGenerator: LogIdGenerator
) {

    private val methodLogger = MethodLogger()

    suspend fun activate() {
        methodLogger.logSuspendMethod(::activate) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to REMOVE_OLD_DATA.value)
        }?.let { return it() }

        withContext(Dispatchers.IO) {
            val threshold = LocalDateTime.now().minusDays(config.logging.requestLog.maxAgeDays.toLong())
            transaction(database) {
                RequestLog.deleteWhere { inserted.less(threshold) }
            }
        }
    }

    fun scheduleRemoveOldData() {
        val now = ZonedDateTime.now()
        val schedule = Schedule.parse(config.logging.requestLog.schedule)
        val startDelaySec = schedule.next(now).toEpochSecond() - now.toEpochSecond()
        executor.schedule(::removeOldRequests, startDelaySec, TimeUnit.SECONDS)
    }

    private fun removeOldRequests() {
        GlobalScope.launch { activate() }
        scheduleRemoveOldData()
    }
}