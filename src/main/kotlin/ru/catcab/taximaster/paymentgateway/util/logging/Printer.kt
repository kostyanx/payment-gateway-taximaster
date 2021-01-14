package ru.catcab.taximaster.paymentgateway.util.logging

interface Printer {
    val config: LogConfig
    fun logIn(vararg parameters: Any?)
    fun logOut()
    fun logOut(value: Any?)
    fun logException(ex: Throwable)
}