package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import ru.catcab.taximaster.paymentgateway.database.enum.Source
import ru.catcab.taximaster.paymentgateway.database.enum.Status

object Payments : IntIdTable("PAYMENTS", "ID") {
    val sourceType = enumerationByName("SOURCE_TYPE", 20, Source::class)
    val payId = varchar("PAY_ID", 100)
    val receiver = varchar("RECEIVER", 20)
    val amount = decimal("AMOUNT", 18, 2)
    val payTimestamp = datetime("PAY_TIMESTAMP")
    val requestId = varchar("REQUEST_ID", 40)
    val operId = integer("OPER_ID")
    val status = enumerationByName("STATUS", 20, Status::class)
    val counter = integer("COUNTER")
    val errorMessage = text("ERROR_MESSAGE")
    val inserted = datetime("INSERTED")
    val updated = datetime("UPDATED")
}

