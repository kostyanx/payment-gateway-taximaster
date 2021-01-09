package ru.catcab.taximaster.paymentgateway.util.common

object Extensions {
    fun String.md5(): String {
        return Helpers.md5(this)
    }
}