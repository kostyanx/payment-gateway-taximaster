package ru.catcab.taximaster.paymentgateway.util.logging

import org.slf4j.Logger

class DefaultPrinter(
    val log: Logger,
    override val config: LogConfig,
    val parameterNames: List<String>
): Printer {
    override fun logIn(vararg parameters: Any?) {
        if (config.params) {
            val message = buildString {
                append("in")
                parameterNames.forEach { append(" $it={}") }
            }
            log.debug(message, *parameters)
        } else {
            log.debug("in")
        }
    }

    override fun logOut() {
        log.debug("out")
    }

    override fun logOut(value: Any?) {
        if (config.returnVal) {
            log.debug("out value=$value")
        } else {
            log.debug("out")
        }
    }

    override fun logException(ex: Throwable) {
        if (config.error) {
            log.error("exception", ex)
        } else {
            log.error("exception")
        }
    }
}