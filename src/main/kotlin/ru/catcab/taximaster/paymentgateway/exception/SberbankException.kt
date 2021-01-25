package ru.catcab.taximaster.paymentgateway.exception

class SberbankException(
    val code: Int,
    message: String
): RuntimeException(message)