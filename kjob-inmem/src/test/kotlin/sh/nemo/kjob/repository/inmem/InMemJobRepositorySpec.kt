package sh.nemo.kjob.repository.inmem

import io.kotest.assertions.throwables.shouldThrow
import sh.nemo.kjob.internal.scheduler.js
import sh.nemo.kjob.repository.JobRepository
import sh.nemo.kjob.repository.JobRepositoryContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@ExperimentalCoroutinesApi
class InMemJobRepositorySpec : JobRepositoryContract() {

    override val testee: JobRepository = InMemJobRepository(clock)

    override fun randomJobId(): String = UUID.randomUUID().toString()

    private val inmemTestee = testee as InMemJobRepository

    override suspend fun deleteAll() {
        inmemTestee.deleteAll()
    }

    init {
        should("only allow unique job ids") {
            val job = js()

            testee.save(job, null)
            shouldThrow<IllegalArgumentException> {
                testee.save(job, null)
            }
        }

    }

}
