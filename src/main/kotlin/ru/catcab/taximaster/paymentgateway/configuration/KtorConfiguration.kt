package ru.catcab.taximaster.paymentgateway.configuration

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject
import ru.catcab.taximaster.paymentgateway.dto.sberbank.ResponseError
import ru.catcab.taximaster.paymentgateway.exception.SberbankException
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankPaymentOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.RECEIVER
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID



fun Application.module() {

    val sberbankCheckOperation by inject<SberbankCheckOperation>()
    val sberbankPaymentOperation by inject<SberbankPaymentOperation>()
    val logIdGenerator by inject<LogIdGenerator>()

    install(CallLogging) {
        mdc(REQUEST_ID.value) { logIdGenerator.generate() }
        mdc(RECEIVER.value) { it.parameters["ACCOUNT"]?.removeLeadingZeros() }
    }

    install(StatusPages) {
        exception<SberbankException> { e ->
            call.respond(ResponseError(e.code, e.message!!))
        }
        exception<Throwable> { e ->
            call.application.environment.log.error("Unexpected error:", e)
            call.respond(ResponseError(-1, e.toString()))
        }
    }

    install(ContentNegotiation) {
        val xmlMapper = XmlMapper().apply { configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true) }
        val mapper = xmlMapper.registerModule(KotlinModule())
        val converter = JacksonConverter(mapper)
        register(ContentType.Application.Xml, converter)
    }

    routing {

        get("/") {
            throw UnsupportedOperationException("not implemented")
        }
        get("/jbilling/pay/sberbank2") {
            val params = call.parameters
            processSberbankPayment(params, sberbankCheckOperation, sberbankPaymentOperation)
        }
        post("/jbilling/pay/sberbank2") {
            val params = call.receiveParameters()
            processSberbankPayment(params, sberbankCheckOperation, sberbankPaymentOperation)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processSberbankPayment(
    params: Parameters,
    sberbankCheckOperation: SberbankCheckOperation,
    sberbankPaymentOperation: SberbankPaymentOperation
) {
    val action = requireNotNull(params["ACTION"], { "ACTION parameter not defined" })
    val account = requireNotNull(params["ACCOUNT"], { "ACCOUNT parameter not defined" })
    when (action.toLowerCase()) {
        "check" -> {
            val checkResponse = sberbankCheckOperation.activate(account)
            call.respond(checkResponse)
        }
        "payment" -> {
            val amount = requireNotNull(params["AMOUNT"], { "AMOUNT parameter not defined" })
            val payId = requireNotNull(params["PAY_ID"], { "PAY_ID parameter not defined" })
            val payDate = requireNotNull(params["PAY_DATE"], { "PAY_DATE parameter not defined" })
            val payCh = params["PAY_CH"]
            val paymentResponse = sberbankPaymentOperation.activate(account, amount, payId, payDate, payCh)
            call.respond(paymentResponse)
        }
        else -> {
            call.respond(ResponseError(2, "Неизвестный тип запроса"))
        }
    }
}