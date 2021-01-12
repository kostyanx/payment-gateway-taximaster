package ru.catcab.taximaster.paymentgateway.util.logging

import org.slf4j.Logger

class DefaultPrinter(
    val log: Logger,
    val parameterNames: List<String>
): Printer {
    override fun logIn(vararg parameters: Any?) {
        val message = buildString {
            append("in")
            parameterNames.forEach { append(" $it={}") }
        }
        log.debug(message, *parameters)
    }

    override fun logOut() {
        log.debug("out")
    }

    override fun logOut(value: Any?) {
        log.debug("out value=$value")
    }

    override fun logException(ex: Throwable) {
        log.error("exception", ex)
    }
}