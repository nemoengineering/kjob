package sh.nemo.kjob.internal.scheduler

import sh.nemo.kjob.job.JobStatus.*
import sh.nemo.kjob.repository.JobRepository
import sh.nemo.kjob.repository.LockRepository
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory
import java.util.concurrent.ScheduledExecutorService

internal class JobCleanupScheduler(
        executorService: ScheduledExecutorService,
        period: Long,
        private val jobRepository: JobRepository,
        private val lockRepository: LockRepository,
        private val limit: Int
) : SimplePeriodScheduler(executorService, period) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun findAndCleanup() {
        jobRepository.findNext(emptySet(), setOf(SCHEDULED, RUNNING, ERROR), limit).collect { job ->
            val isAlive = job.kjobId?.let { lockRepository.exists(it) } ?: false
            if (!isAlive) {
                val applied = jobRepository.reset(job.id, job.kjobId)
                if (!applied)
                    logger.error("Couldn't reset kjob[${job.id}] with kjob id '${job.kjobId}'")
            }
        }
    }

    fun start(): Unit = run {
        logger.debug("Cleanup scheduled.")
        findAndCleanup()
    }
}
