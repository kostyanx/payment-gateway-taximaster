package ru.catcab.taximaster.paymentgateway.util.logging

import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmName

class MethodLogger {
    private val executed: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
    private val loggerMap = ConcurrentHashMap<Any, Printer>()

    companion object {
        val DEFAULT_CONFIG = LogConfig()
    }

    fun <R> logMethod(method: () -> R, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, config)
        val prevMdc = printer.config.mdc?.let { MDC.getCopyOfContextMap() as Map<String, String> }
        try {
            executed.set(true)
            printer.config.mdc?.also { MDC.setContextMap(prevMdc!!.plus(it)) }
            printer.logIn()
            val result = method()
            if (result != Unit) printer.logOut(result) else printer.logOut()
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            prevMdc?.also { MDC.setContextMap(prevMdc) }
            executed.set(false)
        }
    }


    fun <A, R> logMethod(method: (A) -> R, a: A, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, config)
        val prevMdc = printer.config.mdc?.let { MDC.getCopyOfContextMap() as Map<String, String> }
        try {
            executed.set(true)
            printer.config.mdc?.also { MDC.setContextMap(prevMdc!!.plus(it)) }
            printer.logIn(a)
            val result = method(a)
            if (result != Unit) printer.logOut(result) else printer.logOut()
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            prevMdc?.also { MDC.setContextMap(prevMdc) }
            executed.set(false)
        }
    }

    fun <A, B, R> logMethod(method: (A, B) -> R, a: A, b: B, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, config)
        val prevMdc = printer.config.mdc?.let { MDC.getCopyOfContextMap() as Map<String, String> }
        try {
            executed.set(true)
            printer.config.mdc?.also { MDC.setContextMap(prevMdc!!.plus(it)) }
            printer.logIn(a, b)
            val result = method(a, b)
            if (result != Unit) printer.logOut(result) else printer.logOut()
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            prevMdc?.also { MDC.setContextMap(prevMdc) }
            executed.set(false)
        }
    }

    fun <A, B, C, R> logMethod(method: (A, B, C) -> R, a: A, b: B, c: C, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, config)
        val prevMdc = printer.config.mdc?.let { MDC.getCopyOfContextMap() as Map<String, String> }
        try {
            executed.set(true)
            printer.config.mdc?.also { MDC.setContextMap(prevMdc!!.plus(it)) }
            printer.logIn(a, b, c)
            val result = method(a, b, c)
            if (result != Unit) printer.logOut(result) else printer.logOut()
            return { result }
        } catch (ex: Throwable) {
            printer.logException(ex)
            return { throw ex }
        } finally {
            prevMdc?.also { MDC.setContextMap(prevMdc) }
            executed.set(false)
        }
    }

    fun <A, B, C, D, R> logMethod(method: (A, B, C, D) -> R, a: A, b: B, c: C, d: D, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, config)
        val prevMdc = printer.config.mdc?.let { MDC.getCopyOfContextMap() as Map<String, String> }
        try {
            executed.set(true)
            printer.config.mdc?.also { MDC.setContextMap(prevMdc!!.plus(it)) }
            printer.logIn(a, b, c, d)
            val result = method(a, b, c, d)
            if (result != Unit) printer.logOut(result) else printer.logOut()
            return { result }
        } catch (ex: Throwable) {
            printer.logException(ex)
            return { throw ex }
        } finally {
            prevMdc?.also { MDC.setContextMap(prevMdc) }
            executed.set(false)
        }
    }

    suspend fun <R> logSuspendMethod(method: suspend () -> R, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
            else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn()
                val result = method()
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (e: Throwable) {
                printer.logException(e)
                return@withContext { throw e }
            } finally {
                executed.set(false)
            }
        }
    }

    suspend fun <A, R> logSuspendMethod(method: suspend (A) -> R, a: A, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
        else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn(a)
                val result = method(a)
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    suspend fun <A, B, R> logSuspendMethod(method: suspend (A, B) -> R, a: A, b: B, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
        else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn(a, b)
                val result = method(a, b)
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    suspend fun <A, B, C, R> logSuspendMethod(method: suspend (A, B, C) -> R, a: A, b: B, c: C, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
        else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn(a, b, c)
                val result = method(a, b, c)
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    suspend fun <A, B, C, D, R> logSuspendMethod(method: suspend (A, B, C, D) -> R, a: A, b: B, c: C, d: D, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
        else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn(a, b, c, d)
                val result = method(a, b, c, d)
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    suspend fun <A, B, C, D, E, F, R> logSuspendMethod(method: suspend (A, B, C, D, E, F) -> R, a: A, b: B, c: C, d: D, e: E, f: F, config: (LogConfig.Builder.() -> Unit)? = null): (() -> R)? {
        val printer = getPrinter(method, config)
        val context = if (printer.config.mdc != null)
            executed.asContextElement() + MDCContext(MDC.getCopyOfContextMap().plus(printer.config.mdc!!))
        else executed.asContextElement()
        return withContext(context) {
            if (executed.get()) return@withContext null
            try {
                executed.set(true)
                printer.logIn(a, b, c, d, e, f)
                val result = method(a, b, c, d, e, f)
                if (result != Unit) printer.logOut(result) else printer.logOut()
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    private fun getPrinter(method: Any, config: (LogConfig.Builder.() -> Unit)? = null): Printer {
        return loggerMap.computeIfAbsent(method) {
            method as CallableReference
            val parameterNames = method.valueParameters.map { it.name!! }
            val loggerName = (method.owner as KClass<*>).jvmName + "." + method.name
            val logger = LoggerFactory.getLogger(loggerName)
            val cfg = if (config != null) LogConfig.Builder().apply(config).build() else DEFAULT_CONFIG
            DefaultPrinter(logger, cfg, parameterNames)
        }
    }
}