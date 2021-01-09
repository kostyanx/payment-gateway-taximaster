package ru.catcab.taximaster.paymentgateway.dto.api

interface CommonResponse<T> {
    val code: Int
    val descr: String
    val data: T
}