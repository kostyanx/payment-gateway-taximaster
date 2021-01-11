package ru.catcab.taximaster.paymentgateway.service.dao

import javax.sql.DataSource

class RequestLogDao(
    private val dataSource: DataSource
) {
    fun genNextId(): Long {
        // language=H2
        val sql = "SELECT REQUEST_LOG_SEQUENCE_ID.NEXTVAL AS RESULT";
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { rs ->
                    rs.next()
                    rs.getLong(1)
                }
            }
        }
    }
}