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
import ru.catcab.taximaster.paymentgateway.controller.CcbController
import ru.catcab.taximaster.paymentgateway.controller.SberbankController
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbRequest
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbResponseError
import ru.catcab.taximaster.paymentgateway.dto.sberbank.ResponseError
import ru.catcab.taximaster.paymentgateway.exception.SberbankException
import ru.catcab.taximaster.paymentgateway.logic.RequestLogOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.RECEIVER
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID

val xmlMapper = XmlMapper().apply {
    configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
    registerModule(KotlinModule())
}

fun Application.module() {

    val requestLogOperation by inject<RequestLogOperation>()
    val sberbankController by inject<SberbankController>()
    val ccbController by inject<CcbController>()
    val logIdGenerator by inject<LogIdGenerator>()

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
            GlobalScope.launch(MDCContext()) { requestLogOperation.activate(method, call.request.path(), params, 200, response) }
        }
        exception<Throwable> { e ->
            call.application.environment.log.error("Unexpected error:", e)
            val path = call.request.path()
            val response: Any = when {
                path.trimEnd('/').endsWith("sberbank2") -> ResponseError(-1, e.toString())
                path.trimEnd('/').endsWith("sbrebank") -> CcbResponseError(90, "Временная техническая ошибка")
                else -> ResponseError(-1, e.toString())
            }
            call.respond(response)
            val method = call.request.httpMethod
            val params = if (method == HttpMethod.Get) call.parameters else call.receiveParameters()
            GlobalScope.launch(MDCContext()) { requestLogOperation.activate(method, path, params, 200, response) }
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
            processSberbankGet(sberbankController, requestLogOperation)
        }
        get("/jbilling/pay/sberbank2/") {
            processSberbankGet(sberbankController, requestLogOperation)
        }
        post("/jbilling/pay/sberbank2") {
            processSberbankPost(sberbankController, requestLogOperation)
        }
        post("/jbilling/pay/sberbank2/") {
            processSberbankPost(sberbankController, requestLogOperation)
        }
        post("/jbilling/pay/sberbank") {
            processCcb(ccbController, requestLogOperation)
        }
        post("/jbilling/pay/sberbank/") {
            processCcb(ccbController, requestLogOperation)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processCcb(
    ccbController: CcbController,
    requestLogOperation: RequestLogOperation
) {
    val params = call.receiveParameters()
    val xmlString = params["params"]
    @Suppress("BlockingMethodInNonBlockingContext")
    val ccbRequest = xmlMapper.readValue(xmlString, CcbRequest::class.java)!!
    val account = ccbRequest.params.account
    val mdcMap = if (account != null) mapOf(RECEIVER.value to account) else mapOf()
    val ctx = this
    withContext(MDCContext(MDC.getCopyOfContextMap() + mdcMap)) {
        val response = ccbController.activate(ctx, ccbRequest)
        call.respond(response)
        GlobalScope.launch(MDCContext()) { requestLogOperation.activate(call.request.httpMethod, call.request.path(), params, 200, response) }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processSberbankGet(
    sberbankController: SberbankController,
    requestLogOperation: RequestLogOperation
) {
    val params = call.parameters
    val response = sberbankController.activate(this, params)
    call.respond(response)
    GlobalScope.launch(MDCContext()) { requestLogOperation.activate(call.request.httpMethod, call.request.path(), params, 200, response) }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processSberbankPost(
    sberbankController: SberbankController,
    requestLogOperation: RequestLogOperation
) {
    val params = call.receiveParameters()
    val mdcMap = params["ACCOUNT"]?.let { mapOf(RECEIVER.value to it) } ?: mapOf()
    val ctx = this
    withContext(MDCContext(MDC.getCopyOfContextMap() + mdcMap)) {
        val response = sberbankController.activate(ctx, params)
        call.respond(response)
        GlobalScope.launch(MDCContext()) { requestLogOperation.activate(call.request.httpMethod, call.request.path(), params, 200, response) }
    }
}
