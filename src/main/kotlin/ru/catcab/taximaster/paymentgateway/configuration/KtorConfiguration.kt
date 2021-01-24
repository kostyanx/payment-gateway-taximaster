package ru.catcab.taximaster.paymentgateway.configuration

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASH
import ru.catcab.taximaster.paymentgateway.logic.PaymentInOperation
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import java.time.LocalDateTime

fun Application.module() {

    val paymentInOperation by inject<PaymentInOperation>()
    val logIdGenerator by inject<LogIdGenerator>()

    routing {
        get("/") {
            call.respondText("Hello World!", ContentType.Text.Plain)
            val payId = "pay-id-" + logIdGenerator.generate()
            val requestId = "req-id-" + logIdGenerator.generate()
            paymentInOperation.activate(SBERBANK_CASH, "1", "9.99".toBigDecimal(), payId, LocalDateTime.now(), requestId)
        }
    }
}