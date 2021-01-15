package ru.catcab.taximaster.paymentgateway.util.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class StrategyTest {

    @Test
    fun resolve() {
        /*language=JSON*/
        val strategySpec = """{"intervals": [{"interval": 300, "quantity": 2}, {"interval": 600, "quantity": 2}, {"interval": 1200, "quantity": 2} ]}"""
        val strategy = Json.decodeFromString<Strategy>(strategySpec)
        assertEquals(300, strategy.resolve(0)!!.interval) // negative and zero errors - invalid value for this implementation and returns first of interval
        assertEquals(300, strategy.resolve(1)!!.interval)
        assertEquals(300, strategy.resolve(2)!!.interval)
        assertEquals(600, strategy.resolve(3)!!.interval)
        assertEquals(600, strategy.resolve(4)!!.interval)
        assertEquals(1200, strategy.resolve(5)!!.interval)
        assertEquals(1200, strategy.resolve(6)!!.interval)
        assertNull(strategy.resolve(7))
        assertNull(strategy.resolve(8))
    }
}