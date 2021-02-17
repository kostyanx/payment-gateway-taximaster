package ru.catcab.taximaster.paymentgateway.database.enum

import io.ktor.http.*

enum class RequestMethod {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS;

    companion object {
        fun of(method: HttpMethod) = valueOf(method.value)
        fun HttpMethod.toRequestMethod() = of(this)
    }
}