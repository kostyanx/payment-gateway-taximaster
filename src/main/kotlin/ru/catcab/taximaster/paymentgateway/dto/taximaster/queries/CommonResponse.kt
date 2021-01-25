package ru.catcab.taximaster.paymentgateway.dto.taximaster.queries

interface CommonResponse<T> {
    val code: Int
    val descr: String
    val data: T
}