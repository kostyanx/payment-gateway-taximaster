package ru.catcab.taximaster.paymentgateway.configuration.values

import ru.catcab.taximaster.paymentgateway.util.common.Helpers

data class AuditLogTemplate (
    val path: String,
    val action: String,
    val message: String
) {
    fun resolve(resolver: (String) -> String?) : String {
        return Helpers.resolve(message, false, resolver)
    }
}