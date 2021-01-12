package ru.catcab.taximaster.paymentgateway.util.logging

import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

class MethodLogger {
    private val executed: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
    private val loggerMap = ConcurrentHashMap<Any, Printer>()

    companion object {
        var LOG = LoggerFactory.getLogger(MethodLogger::class.java)!!
    }

    fun logMethod(method: () -> Unit) {
        if (executed.get()) return
        val printer = getPrinter(method)
        try {
            executed.set(true)
            printer.logIn()
            val result = method()
            printer.logOut(result)
        } catch (e: Throwable) {
            printer.logException(e)
        } finally {
            executed.set(false)
        }
    }

    fun <R> logMethod(method: () -> R): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method)
        try {
            executed.set(true)
            printer.logIn()
            val result = method()
            printer.logOut(result)
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            executed.set(false)
        }
    }


    suspend fun <R> logSuspendMethod(method: suspend () -> R): (() -> R)? {
        return withContext(executed.asContextElement(false)) {
            if (executed.get()) return@withContext null
            val printer = getSuspedPrinter(method)
            try {
                executed.set(true)
                printer.logIn()
                val result = method()
                printer.logOut(result)
                return@withContext { result }
            } catch (e: Throwable) {
                printer.logException(e)
                return@withContext { throw e }
            } finally {
                executed.set(false)
            }
        }
    }

    inline fun <reified A, R> logMethod(noinline method: (A) -> R, a: A): (() -> R)? {
        return logMethod1(method, a, A::class)
    }

    fun <A, R> logMethod1(method: (A) -> R, a: A, vararg parameterTypes: KClass<*>): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, *parameterTypes)
        try {
            executed.set(true)
            printer.logIn(a)
            val result = method(a)
            printer.logOut(result)
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            executed.set(false)
        }
    }

    suspend fun <A, R> logSuspendMethod(method: suspend (A) -> R, a: A): (() -> R)? {
        return withContext(executed.asContextElement()) {
            if (executed.get()) return@withContext null
            val printer = getSuspedPrinter(method)
            try {
                executed.set(true)
                printer.logIn(a)
                val result = method(a)
                printer.logOut(result)
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    inline fun <reified A, reified B, R> logMethod(noinline method: (A, B) -> R, a: A, b: B): (() -> R)? {
        return logMethod2(method, a, b, A::class, B::class)
    }

    fun <A, B, R> logMethod2(method: (A, B) -> R, a: A, b: B, vararg parameterTypes: KClass<*>): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, *parameterTypes)
        try {
            executed.set(true)
            printer.logIn(a, b)
            val result = method(a, b)
            printer.logOut(result)
            return { result }
        } catch (e: Throwable) {
            printer.logException(e)
            return { throw e }
        } finally {
            executed.set(false)
        }
    }

    suspend fun <A, B, R> logSuspendMethod(method: suspend (A, B) -> R, a: A, b: B): (() -> R)? {
        return withContext(executed.asContextElement(false)) {
            if (executed.get()) return@withContext null
            val printer = getSuspedPrinter(method)
            try {
                executed.set(true)
                printer.logIn(a, b)
                val result = method(a, b)
                printer.logOut(result)
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    inline fun <reified A, reified B, reified C, R> logMethod(noinline method: (A, B, C) -> R, a: A, b: B, c: C): (() -> R)? {
        return logMethod3(method, a, b, c, A::class, B::class, C::class)
    }

    fun <A, B, C, R> logMethod3(method: (A, B, C) -> R, a: A, b: B, c: C, vararg parameterTypes: KClass<*>): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, *parameterTypes)
        try {
            executed.set(true)
            printer.logIn(a, b, c)
            val result = method(a, b, c)
            printer.logOut(result)
            return { result }
        } catch (ex: Throwable) {
            printer.logException(ex)
            return { throw ex }
        } finally {
            executed.set(false)
        }
    }


    inline fun <reified A, reified B, reified C, reified D, R> logMethod(noinline method: (A, B, C, D) -> R, a: A, b: B, c: C, d: D): (() -> R)? {
        return logMethod4(method, a, b, c, d, A::class, B::class, C::class, D::class)
    }

    fun <A, B, C, D, R> logMethod4(method: (A, B, C, D) -> R, a: A, b: B, c: C, d: D, vararg parameterTypes: KClass<*>): (() -> R)? {
        if (executed.get()) return null
        val printer = getPrinter(method, *parameterTypes)
        try {
            executed.set(true)
            printer.logIn(a, b, c, d)
            val result = method(a, b, c, d)
            printer.logOut(result)
            return { result }
        } catch (ex: Throwable) {
            printer.logException(ex)
            return { throw ex }
        } finally {
            executed.set(false)
        }
    }

    suspend fun <A, B, C, D, E, F, R> logSuspendMethod(method: suspend (A, B, C, D, E, F) -> R, a: A, b: B, c: C, d: D, e: E, f: F): (() -> R)? {
        return withContext(executed.asContextElement(false)) {
            if (executed.get()) return@withContext null
            val printer = getSuspedPrinter(method)
            try {
                executed.set(true)
                printer.logIn(a, b, c, d, e, f)
                val result = method(a, b, c, d, e, f)
                printer.logOut(result)
                return@withContext { result }
            } catch (ex: Throwable) {
                printer.logException(ex)
                return@withContext { throw ex }
            } finally {
                executed.set(false)
            }
        }
    }

    private fun getSuspedPrinter(method: Any): Printer {
        return loggerMap.computeIfAbsent(method) {
            method as CallableReference
            val parameterNames = method.valueParameters.map { it.name!! }
            val loggerName = (method.owner as KClass<*>).jvmName + "." + method.name
            val logger = LoggerFactory.getLogger(loggerName)
            DefaultPrinter(logger, parameterNames)
        }
    }

    private fun getPrinter(method: Any, vararg parameterTypes: KClass<*>): Printer {
        return loggerMap.computeIfAbsent(method) {
            val stackTrace = Throwable().stackTrace
            val logMethodStackElement = stackTrace.first { it.className == this.javaClass.name && it.methodName.startsWith("logMethod") }
            val methodStackElement = stackTrace[stackTrace.indexOf(logMethodStackElement) + 1]
            val loggerName = "${methodStackElement.className}.${methodStackElement.methodName}"
            val logger = LoggerFactory.getLogger(loggerName)!!

            val parameterNames = Reflection.getOrCreateKotlinClass(Class.forName(methodStackElement.className)).declaredMembers
                .filter { it.name == methodStackElement.methodName }
                .first { it.valueParameters.map { it.type.jvmErasure } == parameterTypes.toList() }
                .valueParameters.map { it.name!! }

            DefaultPrinter(logger, parameterNames)
        }
    }
}