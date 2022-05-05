package sh.nemo.kjob.repository.inmem

import io.kotest.matchers.shouldBe
import sh.nemo.kjob.repository.LockRepository
import sh.nemo.kjob.repository.LockRepositoryContract
import sh.nemo.kjob.repository.now
import java.time.Duration

class InMemLockRepositorySpec : LockRepositoryContract() {
    override val testee: LockRepository = InMemLockRepository(clock) {
        expireLockInMinutes = 1
    }

    private val inmemTestee = testee as InMemLockRepository

    override suspend fun deleteAll() {
        inmemTestee.deleteAll()
    }

    init {
        should("return false if lock is expired") {
            val id = id()
            clock.update(now().minus(Duration.ofMinutes(1)))
            testee.ping(id)

            testee.exists(id) shouldBe true
            clock.update(now())
            testee.exists(id) shouldBe false
        }
    }

}
