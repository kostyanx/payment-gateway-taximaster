package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import ru.catcab.taximaster.paymentgateway.database.enum.RequestMethod
import ru.catcab.taximaster.paymentgateway.database.enum.RequestMethod.POST
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.nowExpression

object RequestLog : IntIdTable("REQUEST_LOG", "ID") {
    val requestId = varchar("REQUEST_ID", 40)
    val requestMethod = enumerationByName("REQUEST_METHOD", 10, RequestMethod::class).default(POST)
    val requestUrl = varchar("REQUEST_URL", 255)
    val requestBody = text("REQUEST_BODY")
    val responseCode = integer("RESPONSE_CODE")
    val responseBody = text("RESPONSE_BODY")
    val inserted = datetime("INSERTED").defaultExpression(nowExpression)
}

