package ru.catcab.taximaster.paymentgateway.util.logging

interface Printer {
    fun logIn(vararg parameters: Any?)
    fun logOut()
    fun logOut(value: Any?)
    fun logException(ex: Throwable)
}