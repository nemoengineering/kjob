package sh.nemo.kjob.internal.scheduler

import io.kotest.core.spec.style.ShouldSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import sh.nemo.kjob.job.Lock
import sh.nemo.kjob.repository.LockRepository
import java.time.Instant
import java.util.*

class KeepAliveSchedulerSpec : ShouldSpec() {
    init {
        should("execute 'ping' on every run") {
            val repoMock = mockk<LockRepository>()
            val testee = KeepAliveScheduler(newScheduler(), 30, repoMock)
            val id = UUID.randomUUID()
            coEvery { repoMock.ping(id) } answers { Lock(id, Instant.now()) }
            testee.start(id)

            coVerify(timeout = 50) { repoMock.ping(id) }
        }
    }
}
