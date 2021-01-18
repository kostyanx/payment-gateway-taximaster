package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType
import ru.catcab.taximaster.paymentgateway.database.enum.Status
import ru.catcab.taximaster.paymentgateway.database.enum.Status.NEW
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.nowExpression

object Payments : IntIdTable("PAYMENTS", "ID") {
    val sourceType = enumerationByName("SOURCE_TYPE", 20, SourceType::class)
    val payId = varchar("PAY_ID", 100)
    val receiver = varchar("RECEIVER", 20)
    val amount = decimal("AMOUNT", 18, 2)
    val payTimestamp = datetime("PAY_TIMESTAMP").defaultExpression(nowExpression)
    val requestId = varchar("REQUEST_ID", 40)
    val operId = integer("OPER_ID").nullable()
    val status = enumerationByName("STATUS", 20, Status::class).default(NEW)
    val counter = integer("COUNTER").default(0)
    val errorMessage = text("ERROR_MESSAGE").nullable()
    val inserted = datetime("INSERTED").defaultExpression(nowExpression)
    val updated = datetime("UPDATED").defaultExpression(nowExpression)
}

