package ru.catcab.taximaster.paymentgateway.exception

class TaxiMasterApiClientException(
    val code: Int,
    val descr: String,
    val source: Any
): RuntimeException() {
}