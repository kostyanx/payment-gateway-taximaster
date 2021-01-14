package ru.catcab.taximaster.paymentgateway.util.logging

import org.slf4j.event.Level

data class LogConfig(
    val params: Boolean = true,
    val returnVal: Boolean = true,
    val error: Boolean = true,
    val level: Level = Level.INFO,
    val errorLevel: Level = Level.ERROR,
    val mdc: Map<String, String>? = null
) {
    class Builder {
        var params: Boolean = true
        var returnVal: Boolean = true
        var error: Boolean = true
        var level: Level = Level.INFO
        var errorLevel: Level = Level.ERROR
        var mdc: Map<String, String>? = null

        fun build() = LogConfig(params, returnVal, error, level, errorLevel, mdc)
    }
}