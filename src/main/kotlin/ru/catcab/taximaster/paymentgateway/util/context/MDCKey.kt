package ru.catcab.taximaster.paymentgateway.util.context

import org.slf4j.MDC

enum class MDCKey(
    val value: String
) {
    REQUEST_ID("req_id"),
    OPERATION_ID("oper_id"),
    OPERATION_NAME("oper_name"),
    RECEIVER("receiver");

    companion object {
        @JvmStatic
        fun getValue(key: MDCKey): String {
            return MDC.get(key.value) ?: throw NoSuchElementException("value is not set for key ${key.value}")
        }

        fun <T> with(vararg pairs: Pair<MDCKey, String>, block: () -> T): T {
            pairs.forEach { (key, value) -> MDC.put(key.value, value) }
            return MDCCloseable(pairs.map { (key, _) -> key.value }).use { block() }
        }
    }
}