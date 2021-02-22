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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject
import org.slf4j.MDC
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.dto.sberbank.ResponseError
import ru.catcab.taximaster.paymentgateway.exception.SberbankException
import ru.catcab.taximaster.paymentgateway.logic.RequestLogOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation.Companion.ACCOUNT_NOT_FOUND
import ru.catcab.taximaster.paymentgateway.logic.SberbankPaymentOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.containsAddress
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.RECEIVER
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID

val xmlMapper = XmlMapper().apply {
    configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
    registerModule(KotlinModule())
}

fun Application.module() {

    val sberbankCheckOperation by inject<SberbankCheckOperation>()
    val sberbankPaymentOperation by inject<SberbankPaymentOperation>()
    val requestLogOperation by inject<RequestLogOperation>()
    val logIdGenerator by inject<LogIdGenerator>()
    val config by inject<ApplicationConfig>()


    install(DoubleReceive) { receiveEntireContent = true }

    install(CallLogging) {
        mdc(REQUEST_ID.value) { logIdGenerator.generate() }
        mdc(RECEIVER.value) { it.request.queryParameters["ACCOUNT"]?.removeLeadingZeros() }
    }

    install(StatusPages) {
        exception<SberbankException> { e ->
            val response = ResponseError(e.code, e.message!!)
            call.respond(response)
            val method = call.request.httpMethod
            val params = if (method == HttpMethod.Get) call.parameters else call.receiveParameters()
            GlobalScope.launch { requestLogOperation.activate(method, call.request.path(), params, 200, response) }
        }
        exception<Throwable> { e ->
            call.application.environment.log.error("Unexpected error:", e)
            val response = ResponseError(-1, e.toString())
            call.respond(response)
            val method = call.request.httpMethod
            val params = if (method == HttpMethod.Get) call.parameters else call.receiveParameters()
            GlobalScope.launch { requestLogOperation.activate(method, call.request.path(), params, 200, response) }
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Xml, JacksonConverter(xmlMapper))
    }

    routing {

        get("/") {
            call.respond(ResponseError(-1, "not implemented"))
        }
        get("/jbilling/pay/sberbank2") {
            val params = call.parameters
            processSberbankPayment(call.request.httpMethod, params, config, sberbankCheckOperation, sberbankPaymentOperation, requestLogOperation)
        }
        get("/jbilling/pay/sberbank2/") {
            val params = call.parameters
            println(params.formUrlEncode())
            processSberbankPayment(call.request.httpMethod, params, config, sberbankCheckOperation, sberbankPaymentOperation, requestLogOperation)
        }
        post("/jbilling/pay/sberbank2") {
            val params = call.receiveParameters()
            val mdcMap = params["ACCOUNT"]?.let { mapOf(RECEIVER.value to it) } ?: mapOf()
            withContext(MDCContext(MDC.getCopyOfContextMap() + mdcMap)) {
                processSberbankPayment(call.request.httpMethod, params, config, sberbankCheckOperation, sberbankPaymentOperation, requestLogOperation)
            }
        }
        post("/jbilling/pay/sberbank2/") {
            val params = call.receiveParameters()
            val mdcMap = params["ACCOUNT"]?.let { mapOf(RECEIVER.value to it) } ?: mapOf()
            withContext(MDCContext(MDC.getCopyOfContextMap() + mdcMap)) {
                processSberbankPayment(call.request.httpMethod, params, config, sberbankCheckOperation, sberbankPaymentOperation, requestLogOperation)
            }

        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processSberbankPayment(
    method: HttpMethod,
    params: Parameters,
    config: ApplicationConfig,
    sberbankCheckOperation: SberbankCheckOperation,
    sberbankPaymentOperation: SberbankPaymentOperation,
    requestLogOperation: RequestLogOperation
) {
    val remoteHost = call.request.origin.remoteHost
    val allowed = config.sberbank.security.allowedHosts.contains(remoteHost) || config.sberbank.security.allowedSubnets.any { it.containsAddress(remoteHost) }
    if (!allowed) throw SberbankException(2, "Запрос выполнен с неразрешенного адреса")

    val action = requireNotNull(params["ACTION"], { "ACTION parameter not defined" })
    val account = requireNotNull(params["ACCOUNT"], { "ACCOUNT parameter not defined" })
    val response: Any = when (action.toLowerCase()) {
        "check" -> {
            if (params["SERV"].let { it != null && !config.sberbank.allowedServValues.contains(it) }) throw SberbankException(3, ACCOUNT_NOT_FOUND)
            sberbankCheckOperation.activate(account)
        }
        "payment" -> {
            val amount = requireNotNull(params["AMOUNT"], { "AMOUNT parameter not defined" })
            val payId = requireNotNull(params["PAY_ID"], { "PAY_ID parameter not defined" })
            val payDate = requireNotNull(params["PAY_DATE"], { "PAY_DATE parameter not defined" })
            val payCh = params["PAY_CH"]
            sberbankPaymentOperation.activate(account, amount, payId, payDate, payCh)
        }
        else -> {
            ResponseError(2, "Неизвестный тип запроса")
        }
    }
    call.respond(response)
    GlobalScope.launch(MDCContext()) { requestLogOperation.activate(method, call.request.path(), params, 200, response) }
}