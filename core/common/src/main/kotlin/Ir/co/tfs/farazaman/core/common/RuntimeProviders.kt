package Ir.co.tfs.farazaman.core.common

import kotlinx.coroutines.CoroutineDispatcher

fun interface Clock {
    fun currentTimeMillis(): Long
}

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
