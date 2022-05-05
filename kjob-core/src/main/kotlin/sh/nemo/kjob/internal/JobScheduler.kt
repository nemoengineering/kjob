package sh.nemo.kjob.internal

import sh.nemo.kjob.job.JobSettings
import sh.nemo.kjob.job.ScheduledJob
import sh.nemo.kjob.repository.JobRepository
import org.slf4j.LoggerFactory
import java.time.Instant

interface JobScheduler {
    suspend fun schedule(settings: JobSettings, runAt: Instant? = null): ScheduledJob
}

internal class DefaultJobScheduler(private val jobRepository: JobRepository) : JobScheduler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun schedule(settings: JobSettings, runAt: Instant?): ScheduledJob = settings.run {
        return if (jobRepository.exist(id)) {
            error("Job '$name' with id '$id' has already been scheduled.")
        } else {
            jobRepository.save(this, runAt).also { logger.debug("Scheduled new job '${it.settings.name} @ '$runAt'") }
        }
    }
}
