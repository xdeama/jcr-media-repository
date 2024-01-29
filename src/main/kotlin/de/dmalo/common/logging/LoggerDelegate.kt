package de.dmalo.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

annotation class InjectLogger

class LoggerDelegate {
    private var logger: Logger? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) {
            logger = LoggerFactory.getLogger(thisRef!!.javaClass)
        }
        return logger!!
    }
}
