package ru.catcab.taximaster.paymentgateway.util.context

import org.slf4j.MDC
import java.io.Closeable

class MDCCloseable(private val keys: List<String>) : Closeable {
    override fun close() {
        keys.forEach { key -> MDC.remove(key) }
    }
}