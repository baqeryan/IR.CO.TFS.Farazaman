package Ir.co.tfs.farazaman.core.testing

import Ir.co.tfs.farazaman.core.common.Clock
import Ir.co.tfs.farazaman.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

class FakeClock(initialTimeMillis: Long = 0L) : Clock {
    var now: Long = initialTimeMillis

    override fun currentTimeMillis(): Long = now
}

class TestDispatcherProvider(
    scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
) : DispatcherProvider {
    private val dispatcher = StandardTestDispatcher(scheduler)

    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
