package sh.nemo.kjob

import sh.nemo.kjob.repository.JobRepository
import sh.nemo.kjob.repository.LockRepository
import sh.nemo.kjob.repository.inmem.InMemJobRepository
import sh.nemo.kjob.repository.inmem.InMemLockRepository
import java.time.Clock

class InMemKJob(config: Configuration) : BaseKJob<InMemKJob.Configuration>(config) {

    class Configuration : BaseKJob.Configuration() {
        /**
         * The timeout until a kjob instance is considered dead if no 'I am alive' notification occurred
         */
        var expireLockInMinutes = 5L
    }

    override val jobRepository: JobRepository = InMemJobRepository(Clock.systemUTC())

    override val lockRepository: LockRepository = InMemLockRepository(config, Clock.systemUTC())
}
